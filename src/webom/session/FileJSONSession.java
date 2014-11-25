package webom.session;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileJSONSession implements SessionBackend {
	final static Logger logger = LoggerFactory.getLogger(FileJSONSession.class);
	private final String path;

	// It is very slow too, however works. and more portable.
	public FileJSONSession(String path) {
		this.path = path;
		File root = new File(path);
		try {
			root.mkdirs();
		} catch (Exception ex) {
			logger.error("Could not create session directory, {} cause: {}", path, ex.getMessage());
		}
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
