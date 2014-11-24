package webom.response.transform;

import webom.request.Request;
import webom.response.Response;

public interface ResponseTransofmer<T> {

	public void transform(Request request, Response response, T obj);
}
