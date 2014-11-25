import webom.core.WebOM;
import webom.session.InMemorySession;

public class Test {
	public static void main(String[] args) {
		WebOM server = new WebOM(5000, "webom.examples");
		server.setStaticFileLocation("public");
//		server.setSession(new FileSession("sessions"));
		server.setSession(new InMemorySession());
		server.start();
	}
}
