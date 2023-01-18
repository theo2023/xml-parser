import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ParserTests {

	private Parser sut;
	private String input;

	@Test
	public void empty_input_constructor_initializes_correctly() {
		input = "";
		sut = new Parser(input);
		
		assertEquals(sut.getCurrInputIdx(), 0);
		assertFalse(sut.hasCurrentElement());
		assertEquals(sut.getPath(), "/");
	}
	
	@Test
	public void read_one_element_ignores_leading_and_trailing_whitespace() {
		input = "	  \n \r<foo \t>	 	 </foo    >";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("foo");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		
		sut.readNext(); // closing tag
		
		assertFalse(sut.hasCurrentElement());
		assertEquals(sut.getPath(), "/");
		assertEquals(sut.getCurrInputIdx(), input.length());
	}
	
	@Test
	public void read_one_element_leading_and_trailing_whitespace_stripped_from_data() {
		input = "<name> Theo Steiner	\n</name>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("name");
		expected.setData("Theo Steiner");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(getTopData(), expected.getData());
		
		sut.readNext(); // closing tag
		
		assertFalse(sut.hasCurrentElement());
		assertEquals(sut.getPath(), "/");
		assertEquals(sut.getCurrInputIdx(), input.length());
	}
	
	@Test
	public void read_one_element_with_attribute_list_and_empty_data() {
		input = "<order id=\"1111\"></order>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("order");
		expected.addAttribute("id", "1111");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/order");
	}
	
	@Test
	public void read_two_elements_with_one_a_child_of_the_other() {
		input = "<root><name>Theo Steiner</name></root>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("root");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root");
		
		sut.readNext();
		expected = new Element("name");
		expected.setData("Theo Steiner");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root/name");
		assertEquals(getTopData(), expected.getData());
		
		sut.readNext();
		expected = new Element("root");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root");
		
		sut.readNext();
		
		assertFalse(sut.hasCurrentElement());
		assertEquals(sut.getPath(), "/");
		assertEquals(sut.getCurrInputIdx(), input.length());
	}
	
	@Test
	public void read_one_element_with_comment_in_between() {
		input = "<root><!-- TODO --></root>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("root");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopData(), expected.getData());
		assertEquals(sut.getPath(), "/root");
	}
	
	@Test
	public void read_two_elements_with_multiple_comments() {
		input = "<root><!-- TODO --><name><!-- test -->Theo Steiner</name><!-- hello --></root>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("root");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root");
		
		sut.readNext();
		expected = new Element("name");
		expected.setData("Theo Steiner");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root/name");
		assertEquals(getTopData(), expected.getData());
		
		sut.readNext();
		expected = new Element("root");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/root");
		
		sut.readNext();
		
		assertFalse(sut.hasCurrentElement());
		assertEquals(sut.getPath(), "/");
		assertEquals(sut.getCurrInputIdx(), input.length());
	}
	
	@Test
	public void read_one_element_with_escaped_entity_as_data() {
		input = "<root>&amp;</root>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("root");
		expected.setData("&");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopData(), expected.getData());
		assertEquals(sut.getPath(), "/root");
	}
	
	@Test
	public void read_one_element_with_multiple_escaped_entities_as_data() {
		input = "<root>&amp;&lt;&gt;&quot;&apos;</root>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("root");
		expected.setData("&<>\"'");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopData(), expected.getData());
		assertEquals(sut.getPath(), "/root");
	}
	
	@Test
	public void read_one_element_with_escaped_entity_in_attribute_value() {
		input = "<order id=\"1&amp;11\"></order>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("order");
		expected.addAttribute("id", "1&11");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/order");
	}
	
	@Test
	public void read_one_element_with_multiple_escaped_entities_in_attribute_value() {
		input = "<order id=\"1&amp;1&lt;&gt;1&quot;&apos;\"></order>";
		sut = new Parser(input);
		
		sut.readNext();
		Element expected = new Element("order");
		expected.addAttribute("id", "1&1<>1\"'");
		
		assertEquals(getTopName(), expected.getName());
		assertEquals(getTopAttributesString(), expected.getAttributes().toString());
		assertEquals(sut.getPath(), "/order");
	}
	
	private String getTopName() {
		return sut.getCurrentElement().getName();
	}
	
	private String getTopAttributesString() {
		return sut.getCurrentAttributes().toString();
	}
	
	private String getTopData() {
		return sut.getCurrentData();
	}
}