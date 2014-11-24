package webom.session;

import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

public class Session {
	private String key;
	private boolean destoryCalled = false;

	public HashMap<String,Object> map = new HashMap<>();
	
	public Session(String key) {
		this.key = key;
	}

	public Session() {
		// TODO: Not very safe
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

	public Object get(String key){
		return map.get(key);
	}

	public Set<Entry<String, Object>> getAll(){
		return map.entrySet();
	}
}
