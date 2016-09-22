import java.util.Scanner;

public class Hello {

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String name = scanner.nextLine();
		System.out.println("Hello " + name);
		scanner.close();
	}
}
