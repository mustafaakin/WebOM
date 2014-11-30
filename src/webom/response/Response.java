package webom.response;

import javax.servlet.http.HttpServletResponse;

import webom.util.ContentType;
import webom.util.HTTPStatus;

public class Response {
	private int status = HTTPStatus.OK;
	private String contentType;

	private HttpServletResponse raw;

	public Response(HttpServletResponse raw) {
		this.raw = raw;
	}

	public void addHeader(String name, String value) {
		raw.addHeader(name, value);
	}

	public String getContentType() {
		return contentType;
	}

	public HttpServletResponse getRaw() {
		return raw;
	}

	public int getStatus() {
		return status;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public void setRaw(HttpServletResponse raw) {
		this.raw = raw;
	}

	public void setStatus(int status) {
		this.status = status;
	}

}
