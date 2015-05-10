package tutorial;

public class Test {
	
	public static void main(String[] args) {
		Integer a = 3;
		String b = "", c = "";
		int d = 4;
		Object obj = d;
		System.out.println(Integer.class == a.getClass());
		System.out.println(b.getClass() == c.getClass());
		System.out.println(Number.class.isAssignableFrom(a.getClass()));
		System.out.println(Number.class.isAssignableFrom(obj.getClass()));
		
	}

}
