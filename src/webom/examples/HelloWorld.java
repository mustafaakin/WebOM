package webom.examples;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.request.HTTPRequestHandler;
import webom.request.Request;
import webom.response.Response;

@Handler(method = RequestMethod.GET, path = "/hello")
public class HelloWorld extends HTTPRequestHandler {

	@Override
	public Object handle(Request request, Response response) {
		return "hi";
	}
}
