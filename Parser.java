import java.util.List;
import java.util.ArrayList;
import java.util.EmptyStackException;
import java.util.Stack;
import java.util.Map;

public class Parser {
	
	private String input;
	private int currInputIdx;
	private Stack<Element> stack;
	private List<String> path;
	static enum NextInputType { ELEMENT, DATA, CLOSING_TAG, ALT_CLOSING };
	
	public Parser(String input) {
		this.input = input;
		this.currInputIdx = 0;
		this.stack = new Stack<Element>();
		this.path = new ArrayList<String>();
	}
	
	public NextInputType readNext() {
		if (currInputIdx >= input.length()) {
			throw new StringIndexOutOfBoundsException("No input left to read");
		}
		
		if (currChar() == '<' && nextChar() == '?') { // detect prolog, only appears at beginning of file
			consumeProlog();
		}
		
		consumeWhitespace(); // strip leading whitespace
		consumeComment();
		consumeWhitespace();
		
		if (currChar() == '<' && nextChar() == '/') { // detect closing tag
			return readClosingTag();
		} else if (currChar() == '<' && nextChar() != '!') { // detect element
			return readNextElement();
		}
		// else, in between tags there is data belonging to the current element
		return readData(getCurrentElement());
	}
	
	private NextInputType readNextElement() {
		currInputIdx++; // consume '<'

		String name = "";
		while (currChar() != '>' && currChar() != ' ' && currChar() != '/') { // consume element name
			name += currChar();
			currInputIdx++;
		}

		consumeWhitespace();
		Element newElt = new Element(name);

		if (currChar() != '>' && currChar() != '/') {
			readAttributesList(newElt);
		}
		
		if (currChar() == '>') {
			currInputIdx++; // consume '>'
			// only push if there will be a separate closing tag, not within the opening tag
			stack.push(newElt);
			path.add(name);
		}
		
		if (currChar() == '/' && nextChar() == '>') {
			currInputIdx += 2; // consume "/>"
			return NextInputType.ALT_CLOSING;
		}
		
		consumeComment();
		consumeWhitespace();
		
		if (currChar() == '<' && nextChar() != '/' && nextChar() != '!') { // detect child element
			consumeWhitespace();
			return NextInputType.ELEMENT;
		} else if (currChar() == '<' && nextChar() == '/') { // detect closing tag
			consumeWhitespace();
			return NextInputType.CLOSING_TAG;
		}
		// read data and update element
		return readData(newElt);
	}
	
	private NextInputType readClosingTag() {
		stack.pop();
		path.remove(path.size() - 1);
		
		while (currChar() != '>') {
			currInputIdx++;
		}
		currInputIdx++; // consume '>'
		return NextInputType.CLOSING_TAG;
	}
	
	private NextInputType readData(Element elt) {
		String data = "";
		while (currChar() != '<') { // consume data
			String replaced = detectAndReplaceEscapedChars();
			data += replaced;
			
			if (replaced.equals("")) {
				data += currChar();
				currInputIdx++;
			}
		}
		data = data.stripTrailing();
		elt.appendToData(data);
		return NextInputType.DATA;
	}
	
	private void readAttributesList(Element elt) {
		while (currChar() != '>' && currChar() != '/') {
			String key = "";
			while (currChar() != '=') { // consume attribute name
				key += currChar();
				currInputIdx++;
			}
			key = key.stripTrailing();
			currInputIdx++; // consume '='
			consumeWhitespace();
			currInputIdx++; // consume '"'
			
			String value = "";
			while (currChar() != '"') { // consume attribute value
				String replaced = detectAndReplaceEscapedChars();
				value += replaced;
				
				if (replaced.equals("")) {
					value += currChar();
					currInputIdx++;
				}
			}
			currInputIdx++; // consume '"'
			elt.addAttribute(key, value);
			consumeWhitespace();
		}
	}
	
	private void consumeWhitespace() {
		while (Character.isWhitespace(currChar())) {
			currInputIdx++;
		}
	}
	
	private void consumeProlog() {
		currInputIdx += 2; // consume "<?"
		while (currChar() != '?' || nextChar() != '>') {
			currInputIdx++;
		}
		currInputIdx += 2; // consume "?>"
	}
	
	private void consumeComment() {
		if (currChar() == '<' && nextChar() == '!') { // detect comment opening
			currInputIdx += 4; // consume "<!--"
			while (currChar() != '-') {
				currInputIdx++;
			}
			currInputIdx++; // consume '-'
			if (currChar() == '-' && nextChar() == '>') { // detect comment closing
				currInputIdx += 2; // consume "->"
			}
		}
	}
	
	// this method needs to return the consumed characters to avoid parsing '<' as the beginning of a new element
	private String detectAndReplaceEscapedChars() {
		String replaced = "";
		while (currChar() == '&' && (nextChar() == 'a' || nextChar() == 'l'
			   || nextChar() == 'g' || nextChar() == 'q')) { // detect escaped entity
			currInputIdx++; // consume '&'
			
			String escapedName = "";
			while (currChar() != ';') {
				escapedName += currChar();
				currInputIdx++;
			}
			
			switch (escapedName) {
				case "amp":
					replaced += '&';
					break;
				case "lt":
					replaced += '<';
					break;
				case "gt":
					replaced += '>';
					break;
				case "quot":
					replaced += '"';
					break;
				case "apos":
					replaced += '\'';
					break;
			}
			currInputIdx++; // consume ';'
		}
		return replaced;
	}
	
	private char currChar() {
		if (currInputIdx >= input.length()) {
			throw new StringIndexOutOfBoundsException("Tried to access character beyond input string length");
		}
		return input.charAt(currInputIdx);
	}
	
	private char nextChar() {
		if (currInputIdx >= input.length()) {
			throw new StringIndexOutOfBoundsException("Tried to peek beyond input string length");
		}
		return input.charAt(currInputIdx + 1);
	}
	
	public int getCurrInputIdx() {
		return this.currInputIdx;
	}

	public boolean hasCurrentElement() {
		return !this.stack.isEmpty();
	}
	
	public Element getCurrentElement() {
		if (!hasCurrentElement()) {
			throw new EmptyStackException();
		}
		return this.stack.peek();
	}
	
	public Map<String, String> getCurrentAttributes() {
		return getCurrentElement().getAttributes();
	}
	
	public String getCurrentData() {
		return getCurrentElement().getData();
	}
	
	public String getPath() {
		String result = "/";
		for (int i = 0; i < path.size(); i++) {
			String curr = path.get(i);
			result += (i != path.size() - 1) ? curr + "/" : curr;
		}
		return result;
	}
}