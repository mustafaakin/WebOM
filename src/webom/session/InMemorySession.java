package webom.session;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemorySession implements SessionBackend {
	final static Logger logger = LoggerFactory.getLogger(InMemorySession.class);
	
	HashMap<String, Session> map = new HashMap<>();

	@Override
	public Session get(String sessionKey) {
		return map.get(sessionKey);
	}

	@Override
	public void set(Session session) {
		map.put(session.getKey(), session);
	}

	@Override
	public void destroy(Session session) {
		map.remove(session.getKey());		
	}
}
