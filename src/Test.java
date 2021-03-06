import webom.core.Reporter;
import webom.core.WebOM;
import webom.response.transform.StaticFileHandler.Template;
import webom.session.RedisJSONSession;

public class Test {
	public static void main(String[] args) {
		Reporter.setReporter("http://localhost:8086", "root", "root", "webom");
		WebOM server = new WebOM(8000, "webom.examples");
		server.setStaticFileLocation("public", Template.JADE);
		// server.setSession(new FileSession("sessions"));
		server.setSession(new RedisJSONSession("localhost"));
		server.start();
	}
}
