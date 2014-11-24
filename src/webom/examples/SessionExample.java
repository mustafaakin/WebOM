package webom.examples;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.annotations.validation.Param;
import webom.annotations.validation.Type;
import webom.request.HTTPRequestHandler;
import webom.request.Request;
import webom.response.Response;
import webom.session.Session;

@Handler(method = RequestMethod.GET, path = "/sessionTest")
public class SessionExample extends HTTPRequestHandler {
	@Param(type=Type.SESSION)
	public int visitCount = 0;

	@Override
	public Object handle(Request request, Response response) {
		Session s = request.getSession();
		s.put("visitCount", visitCount + 1);
		return "You have visited " + visitCount + " times.";
	}
}
