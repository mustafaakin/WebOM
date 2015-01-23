package webom.request;

import java.lang.reflect.Method;
import java.util.HashMap;

import webom.annotations.request.WebSocketKey;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public abstract class KeyValueWebSocketHandler extends WebSocketHandler {
	private HashMap<String, Method> mappedMethods = new HashMap<>();

	@Override
	public void onWebSocketConnect() {
		// Register the functions once
		Class<? extends KeyValueWebSocketHandler> cls = this.getClass();
		Method[] methods = cls.getMethods();
		for (Method method : methods) {
			WebSocketKey wsk = method.getAnnotation(WebSocketKey.class);
			if (wsk != null) {
				String key = wsk.key();
				mappedMethods.put(key, method);
			}
		}
	}

	@Override
	public void onWebSocketText(String message) {
		// Parse the JSON message
		try {
			JsonObject json = new JsonParser().parse(message).getAsJsonObject();
			String key = json.get("key").getAsString();
			
			Method method = mappedMethods.get(key);
			if (method == null) {
				onUnknownWebSocketText(message);
				return;
			} else {
				JsonElement value = json.get("value");
				Gson gson = new Gson();
				Class<?> cls = method.getParameterTypes()[0]; 
				// First parameter matters only, must be ensured in onWebSOcketConnect while constructing mappedMethods
				Object valueObject = gson.fromJson(value, cls);
				method.invoke(this, valueObject);
			}

		} catch (Exception ex) {
			// Invalid json
			// Extend these exceptions for others
			onUnknownWebSocketText(message);
		}
	}

	public abstract void onUnknownWebSocketText(String message);
	
	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
	
	}
	
	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		// TODO Auto-generated method stub
		
	}
	
	

}
