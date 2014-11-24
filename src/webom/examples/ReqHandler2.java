package webom.examples;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.annotations.validation.NotNull;
import webom.annotations.validation.Param;
import webom.request.HTTPRequestHandler;
import webom.request.Request;
import webom.response.Response;

@Handler(method = RequestMethod.GET, path = "/hello/:person")
public class ReqHandler2 extends HTTPRequestHandler {
	@NotNull
	@Param
	public String person;

	@Override
	public Object handle(Request request, Response response) {
		return "hello " + person;
	}

}
