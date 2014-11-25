package webom.session;

public interface SessionBackend {
	public void destroy(Session session);

	public Session get(String sessionKey);

	public void set(Session session);
}
