package webom.request;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import webom.session.Session;

public class Request {
	String path;
	String queryString;
	String referrer;
	Cookie[] cookies;
	int contentLength;
	HttpServletRequest raw;

	private Session session;

	public Request(HttpServletRequest raw, Session session) {
		queryString = raw.getQueryString();
		cookies = raw.getCookies();
		contentLength = raw.getContentLength();
		this.session = session;
		this.raw = raw;
	}

	public Session getSession() {
		return session;
	}

	public int getContentLength() {
		return contentLength;
	}

	public Cookie[] getCookies() {
		return cookies;
	}

	public String getPath() {
		return path;
	}

	public String getQueryString() {
		return queryString;
	}

	public HttpServletRequest getRaw() {
		return raw;
	}

	public String getReferrer() {
		return referrer;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setQueryString(String queryString) {
		this.queryString = queryString;
	}

	public void setRaw(HttpServletRequest raw) {
		this.raw = raw;
	}

	public void setReferrer(String referrer) {
		this.referrer = referrer;
	}
}
