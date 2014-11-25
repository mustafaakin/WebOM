package webom.core;

import java.util.HashMap;
import java.util.Map;

public class Route {
	public static String[] getComponentsFromPath(String path) {
		String[] components = path.split("/");
		String[] survivals = new String[components.length];
		int idx = 0;

		for (String s : components) {
			if (s.length() == 0) {
				// Empty
				continue;
			} else if (s.indexOf(" ") != -1) {
				// Spaces
				s = s.replaceAll(" ", "");
				if (s.length() == 0) {
					continue;
				}
			} else {
				// No correction
			}
			survivals[idx++] = s;
		}

		String[] filtered = new String[idx];
		for (int i = 0; i < idx; i++) {
			filtered[i] = survivals[i];
		}
		return filtered;
	}

	public String path;
	public String[] components;
	public boolean[] parametric;

	public Class<?> requestHandlerCls;

	public Route(String path, Class<?> requestHandlerCls) {
		this.requestHandlerCls = requestHandlerCls;
		this.path = path;
		if (path.equals("/")) {
			components = new String[0];
			parametric = new boolean[0];
		} else {
			// Eliminate the empty ones
			components = getComponentsFromPath(path);
			parametric = new boolean[components.length];

			for (int i = 0; i < components.length; i++) {
				if (components[i].startsWith(":")) {
					parametric[i] = true;
					components[i] = components[i].substring(1);
				} else {
					parametric[i] = false;
					components[i] = components[i];
				}
			}
		}
	}

	public Map<String, String> matches(String path) {
		String[] components = getComponentsFromPath(path);

		if (components.length != this.components.length) {
			return null;
		} else {
			HashMap<String, String> map = new HashMap<String, String>();

			for (int i = 0; i < components.length; i++) {
				if (parametric[i]) {
					map.put(this.components[i], components[i]);
					continue;
				} else {
					if (this.components[i].equals(components[i])) {
						continue;
					} else {
						return null;
					}
				}
			}
			return map;
		}
	}
}