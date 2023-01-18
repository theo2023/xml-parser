import java.util.EmptyStackException;
import java.util.Map;
import java.util.HashMap;

public class Example {
	public static void main(String[] args) {
		String input = "<root><order id=\"1111\"><amount>150</amount></order><order id=\"222\">"
					 + "<amount>2</amount></order><order id=\"333\"><amount>2000</amount></order></root>";
		Parser parser = new Parser(input);
		
		Map<String, Integer> idToAmount = new HashMap<String, Integer>();
		
		String id = null;
		Integer amount = null;
		
		// get order ids with amount > 100
		while (parser.getCurrInputIdx() < input.length()) {
			parser.readNext();
			Element currElt = null;
			try {
				currElt = parser.getCurrentElement();
			} catch (EmptyStackException e) {
				break;
			}
			
			if (!currElt.getAttributes().isEmpty()) {
				id = currElt.getAttributes().get("id");
			}
			if (currElt.getData() != null) {
				amount = Integer.parseInt(currElt.getData());
			}
			
			if (amount != null && amount > 100) {
				idToAmount.put(id, amount);
				id = null;
				amount = null;
			}
		}
		for (String ID : idToAmount.keySet()) {
			System.out.print("id: " + ID + " amount: " + idToAmount.get(ID) + "\n");
		}
	}
}