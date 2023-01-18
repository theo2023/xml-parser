import java.util.Scanner;
import java.util.EmptyStackException;

public class Runner {
	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);
		
		System.out.println("Welcome to the interactive XML parser!");
		System.out.println("Paste your raw XML text here: ");
		
		String input = scan.nextLine();
		Parser parser = new Parser(input);
		
		System.out.print("Parse next element? (y) ");
		char command = scan.next().charAt(0);
		do {
			Parser.NextInputType nextInputType = parser.readNext();
			Element currElt = null;
			try {
				currElt = parser.getCurrentElement();
			} catch (EmptyStackException e) {
				break;
			}
			
			if (/*(nextInputType == Parser.NextInputType.ELEMENT || nextInputType == Parser.NextInputType.DATA)
					&&*/ !parser.getCurrentAttributes().isEmpty()) {
				System.out.println("\nCurrent path: " + parser.getPath());
				System.out.print("This element has attribute(s). Press r to read or s to skip. ");
				readOrSkipDetails(scan.next().charAt(0), false, currElt);
				System.out.print("\nContinue parsing? (y) ");
				command = scan.next().charAt(0);
			}
			
			System.out.println("\nCurrent path: " + parser.getPath());
			
			switch (nextInputType) {
				case ELEMENT:
					System.out.print("Child element(s) detected. Continue parsing? (y) ");
					command = scan.next().charAt(0);
					break;
				case DATA:
					System.out.print("Data detected. Press r to read or s to skip. ");
					readOrSkipDetails(scan.next().charAt(0), true, currElt);
					System.out.print("\nContinue parsing? (y) ");
					command = scan.next().charAt(0);
					break;
				case CLOSING_TAG:
					System.out.print("Element fully parsed. Continue parsing? (y) ");
					command = scan.next().charAt(0);
					break;
			}
		} while (command == 'y' && parser.getCurrInputIdx() < input.length());
		
		System.out.println("\nNo further input detected. Parsing is complete.");
		scan.close();
	}
	
	private static void readOrSkipDetails(char command, boolean isData, Element elt) {
		if (command == 'r') {
			if (isData) {
				System.out.println(elt.getData());
			} else { // attributes
				System.out.println(elt.getAttributes());
			}
		}
	}
}