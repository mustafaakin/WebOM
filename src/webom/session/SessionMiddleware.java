package webom.session;

public interface SessionMiddleware {
	public Session get(String sessionKey);
	public void set(Session session);	
}
