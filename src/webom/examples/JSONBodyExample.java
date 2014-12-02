package webom.examples;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.annotations.validation.Param;
import webom.annotations.validation.Type;
import webom.request.HTTPRequestHandler;
import webom.request.Request;
import webom.response.Response;
import webom.session.Session;

@Handler(method = RequestMethod.POST, path = "/jsonTest")
public class JSONBodyExample extends HTTPRequestHandler {
	class Body {
		String name;
		int age;
	}
	
	@Param(type = Type.JSON)
	public Body body;

	
	@Override
	public Object handle(Request request, Response response) {
		return String.format("Hi %s - %d", body.name, body.age);
	}
}
