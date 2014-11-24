package webom.session;

import java.util.HashMap;

public class InMemorySession implements SessionMiddleware {
	HashMap<String, Session> map = new HashMap<>();

	@Override
	public Session get(String sessionKey) {
		return map.get(sessionKey);
	}

	@Override
	public void set(Session session) {
		map.put(session.getKey(), session);
	}
}
