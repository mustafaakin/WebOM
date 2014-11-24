import webom.core.WebOM;

public class Test {
	public static void main(String[] args) {
		WebOM server = new WebOM(5000, "webom.examples");
		server.setStaticFileLocation("public");
		server.start();
	}
}
