import java.util.EmptyStackException;
import java.io.*;

public class Main {
	@SuppressWarnings("resource")
	public static byte[] getBytesFromFile(File file) throws IOException {
		InputStream is = new FileInputStream(file);

		// Get the size of the file
		long length = file.length();

		// You cannot create an array using a long type.
		// It needs to be an int type.
		// Before converting to an int type, check
		// to ensure that file is not larger than Integer.MAX_VALUE.
		if (length > Integer.MAX_VALUE) {
			// File is too large
			throw new IOException("File " + file.getName() + " is too large");
		}

		// Create the byte array to hold the data
		byte[] bytes = new byte[(int)length];

		// Read in the bytes
		int offset = 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
			offset += numRead;
		}

		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file " + file.getName());
		}

		// Close the input stream and return bytes
		is.close();
		return bytes;
	}

	public static void main(String[] args) {
		String input = null;
		try {
			input = new String(getBytesFromFile(new File("C:\\temp\\standard.xml")));
		} catch (Exception e) {
			System.out.println("Could not create string from XML file");
		}
		
		Parser parser = new Parser(input);
		while (parser.getCurrInputIdx() < input.length()) {
			parser.readNext();
			Element currElt = null;
			try {
				currElt = parser.getCurrentElement();
			} catch (EmptyStackException e) {
				break;
			}
			System.out.println(currElt.getName());
		}
	}
}