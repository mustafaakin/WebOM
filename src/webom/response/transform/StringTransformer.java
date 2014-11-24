package webom.response.transform;

import java.io.IOException;

import webom.request.Request;
import webom.response.Response;

public class StringTransformer implements ResponseTransofmer<String> {
	@Override
	public void transform(Request request, Response response, String obj) {
		try {
			response.getRaw().getWriter().write(obj);
			response.getRaw().setContentLength(obj.length());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
