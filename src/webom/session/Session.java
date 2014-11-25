package webom.session;

import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import com.cedarsoftware.util.io.JsonReader;
import com.cedarsoftware.util.io.JsonWriter;

public class Session {
	private String key;
	private boolean destoryCalled = false;
	private SessionBackend backend;

	public void setMap(HashMap<String, Object> map) {
		if ( map != null){
			this.map = map;			
		}
	}
	
	private HashMap<String, Object> map = new HashMap<>();

	public Session(String key, SessionBackend backend) {
		this.key = key;
		this.backend = backend;
	}

	public Session(SessionBackend backend) {
		this.backend = backend;
		// TODO: This is not very safe, replace it with something
		this.key = UUID.randomUUID().toString();
	}

	public boolean isDestroyCalled() {
		return destoryCalled;
	}

	public String getKey() {
		return key;
	}

	public void destroy() {
		destoryCalled = true;
	}

	public Object get(String key) {
		return map.get(key);
	}

	public void put(String key, Object object) {
		map.put(key, object);
	}

	public void fromJSON(String str) {
		try {
			Object obj = JsonReader.jsonToJava(str);
			map = (HashMap<String, Object>) obj;
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	public void save() {
		backend.set(this);
	}

	public HashMap<String, Object> getMap() {
		return map;
	}

	// Use if desired
	public String toJSON() {
		String str = null;
		try {
			str = JsonWriter.objectToJson(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return str;
	}
}
