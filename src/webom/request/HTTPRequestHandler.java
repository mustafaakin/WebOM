package webom.request;

import webom.response.Response;

public abstract class HTTPRequestHandler extends POJOBuilder {
	public abstract Object handle(Request request, Response response);
}
