package tutorial;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;

class Product{
	public String name;
	public String key;
	public double price;
	public String[] items;

	public Product(String name, String key, double price, String[] items) {
		this.name = name;
		this.key = key;
		this.price = price;
		this.items = items;
	}
	
}

public class JaxbJson {
	public static void main(String[] args) {

        Product product = new Product("Banana", "123", 23.00, new String[0]);//{"item1", "item2"});
        XStream xstream = new XStream(new JsonHierarchicalStreamDriver());
//        xstream.addImplicitCollection(Product.class, "items");
        xstream.setMode(XStream.NO_REFERENCES);
        xstream.alias("", Product.class);

        System.out.println(xstream.toXML(product).replace("\n", "").replaceAll(" +", " "));		
		
	}
}
