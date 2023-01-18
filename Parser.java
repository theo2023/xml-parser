import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Map;

public class Parser {
	
	private StringBuilder input;
	private int currInputIdx;
	private Stack<Element> stack;
	private List<String> path;
	static enum NextInputType { ELEMENT, DATA, CLOSING_TAG };
	
	public Parser(String input) {
		this.input = new StringBuilder(input);
		this.currInputIdx = 0;
		this.stack = new Stack<Element>();
		this.path = new ArrayList<String>();
	}
	
	public NextInputType readNext() {
		if (currInputIdx >= input.length()) {
			throw new StringIndexOutOfBoundsException("No input left to read");
		}
		consumeWhitespace(); // strip leading whitespace
		consumeComment();
		if (currChar() == '<' && nextChar() == '/') { // detect closing tag
			return readClosingTag();
		} else if (currChar() == '<' && nextChar() != '!') { // detect element
			return readNextElement();
		}
		return null;
	}
	
	private NextInputType readNextElement() {
		currInputIdx++; // consume '<'

		String name = "";
		while (currChar() != '>' && currChar() != ' ') { // consume element name
			name += currChar();
			currInputIdx++;
		}

		consumeWhitespace();
		Element newElt = new Element(name);

		if (currChar() != '>') {
			readAttributesList(newElt);
		}
		currInputIdx++; // consume '>'
		consumeComment();

		stack.push(newElt);
		path.add(name);

		consumeWhitespace();
		if (currChar() == '<' && nextChar() != '/' && nextChar() != '!') { // detect child element
			consumeWhitespace();
			return NextInputType.ELEMENT;
		} else if (currChar() == '<' && nextChar() == '/') { // detect closing tag
			consumeWhitespace();
			return NextInputType.CLOSING_TAG;
		}
		// read data and update element
		readData(newElt);
		consumeWhitespace();
		return NextInputType.DATA;
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
	
	private void readData(Element elt) {
		String data = "";
		while (currChar() != '<' || nextChar() != '/') { // consume data
			detectAndReplaceEscapedChars();
			data += currChar();
			currInputIdx++;
		}
		data = data.stripTrailing();
		elt.setData(data);
	}
	
	private void readAttributesList(Element elt) {
		while (currChar() != '>') {
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
				detectAndReplaceEscapedChars();
				value += currChar();
				currInputIdx++;
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
	
	private void detectAndReplaceEscapedChars() {
		int origIdx = currInputIdx;
		while (currChar() == '&' && (nextChar() == 'a' || nextChar() == 'l'
			   || nextChar() == 'g' || nextChar() == 'q')) { // detect escaped entity
			int replacementIdx = input.indexOf("&", currInputIdx);
			currInputIdx++; // consume '&'
			
			String escapedName = "";
			while (currChar() != ';') {
				escapedName += currChar();
				currInputIdx++;
			}
			
			switch (escapedName) {
				case "amp":
					input.replace(replacementIdx, currInputIdx + 1, "&");
					break;
				case "lt":
					input.replace(replacementIdx, currInputIdx + 1, "<");
					break;
				case "gt":
					input.replace(replacementIdx, currInputIdx + 1, ">");
					break;
				case "quot":
					input.replace(replacementIdx, currInputIdx + 1, "\"");
					break;
				case "apos":
					input.replace(replacementIdx, currInputIdx + 1, "'");
					break;
			}
			currInputIdx -= escapedName.length();
		}
		currInputIdx = origIdx; // to make it easier to read data
	}
	
	private char currChar() {
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