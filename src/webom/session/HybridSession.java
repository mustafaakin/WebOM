package webom.session;

import java.io.File;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HybridSession implements SessionBackend {
	final static Logger logger = LoggerFactory.getLogger(HybridSession.class);
	private final String path;
	private HashMap<String,Session> map = new HashMap<>();

	// TODO: Periodically save to file, upon creating, load from file..
	public HybridSession(String path) {
		
		this.path = path;
		File root = new File(path);
		try {
			root.mkdirs();
		} catch (Exception ex) {
			logger.error("Could not create session directory, {} cause: {}", path, ex.getMessage());
		}
		throw new UnsupportedOperationException("This is not implemented yet");
	}

	@Override
	public void destroy(Session session) {
		File file = getFile(session.getKey());
		try {
			file.delete();
		} catch (Exception ex) {
			logger.error("Could not delete session {} , error: {}", session.getKey(), ex.getMessage());
		}
	}

	@Override
	public Session get(String key) {
		File file = getFile(key);
		try {
			Session session = new Session(key, this);
			if (!file.exists()) {
				return session;
			}
			String json = FileUtils.readFileToString(file);
			session.fromJSON(json);
			return session;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	private File getFile(String key) {
		return new File(path, key);
	}

	@Override
	public void set(Session session) {
		File file = getFile(session.getKey());
		try {
			FileUtils.writeStringToFile(file, session.toJSON());
		} catch (Exception e) {
			logger.error("Could not write session {} to file, error: {}", session.getKey(), e.getMessage());
		}
	}
}
