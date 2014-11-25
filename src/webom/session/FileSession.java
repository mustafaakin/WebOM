package webom.session;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSession implements SessionBackend {
	final static Logger logger = LoggerFactory.getLogger(FileSession.class);
	private final String path;

	// It is very slow, however works.
	public FileSession(String path) {
		this.path = path;
		File root = new File(path);
		try {
			root.mkdirs();
		} catch (Exception ex) {
			logger.error("Could not create session directory, {} cause: {}", path, ex.getMessage());
		}
	}

	@Override
	public Session get(String key) {
		File file = getFile(key);
		try {
			Session session = new Session(key, this);
			if ( !file.exists()){
				return session;
			}
			
			FileInputStream fileIn = new FileInputStream(file);
			ObjectInputStream in = new ObjectInputStream(fileIn);
			
			Object readObj = in.readObject();			
			session.setMap((HashMap<String, Object>) readObj);
			in.close();
			return session;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	@Override
	public void set(Session session) {
		File file = getFile(session.getKey());
		try {
			FileOutputStream fos = new FileOutputStream(file);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(session.getMap());
			out.close();
		} catch (IOException e) {
			logger.error("Could not write session {} to file, error: {}", session.getKey(), e.getMessage());
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

	private File getFile(String key) {
		return new File(path, key);
	}
}
