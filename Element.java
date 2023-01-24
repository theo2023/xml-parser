import java.util.Map;
import java.util.HashMap;

public class Element {
	
	private String name;
	private Map<String, String> attributes;
	private String data;
	
	public Element(String name) {
		this.name = name;
		this.attributes = new HashMap<String, String>();
		this.data = "";
	}
	
	public String getName() {
		return this.name;
	}
	
	public Map<String, String> getAttributes() {
		return this.attributes;
	}
	
	public String getData() {
		return this.data;
	}
	
	public void appendToData(String data) {
		this.data += data;
	}
	
	public String addAttribute(String key, String value) {
		return this.attributes.put(key, value);
	}
}