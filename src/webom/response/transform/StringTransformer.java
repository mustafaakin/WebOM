package webom.response.transform;

import java.io.IOException;

import webom.request.Request;
import webom.response.Response;

public class StringTransformer implements ResponseTransofmer<String> {
	@Override
	public void transform(Request request, Response response, String obj) {
		try {
			// response.getRaw().setContentLength(obj.length() + 1); // TODO: is
			// it correct?
			response.getRaw().getWriter().write(obj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
