package webom.examples;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.request.HTTPRequestHandler;
import webom.request.Request;
import webom.response.Response;

@Handler(method = RequestMethod.GET, path = "/errorTest")
public class ErrorExample extends HTTPRequestHandler {
	@Override
	public Object handle(Request request, Response response) {
		int a = 5 / 0;
		return "Hello you will not even see this";
	}
}
