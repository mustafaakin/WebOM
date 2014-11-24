package webom.session;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import com.google.gson.Gson;

public class Session {
	private String key;
	private boolean destoryCalled = false;
	private SessionBackend backend;

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

	public void put(String key, Object object){
		map.put(key, object);
	}

	public boolean fromJSON(String str) {
		Gson gson = new Gson();
		HashMap<String, Object> map = (HashMap<String, Object>) gson.fromJson(str, HashMap.class);
		if (map != null) {
			this.map = map;
			return true;
		} else {
			return false;
		}
	}
	
	public void save(){
		backend.set(this);
	}
	
	// Use if desired
	public String toJSON(){
		Gson gson = new Gson();
		return gson.toJson(map);
	}
}
