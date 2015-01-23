package webom.request;

import java.io.IOException;

import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public abstract class WebSocketHandler extends POJOBuilder {
	Session session;
	RemoteEndpoint client;

	public RemoteEndpoint getRemote() {
		return session.getRemote();
	}

	public Session getSession() {
		return session;
	}

	public abstract void onWebSocketBinary(byte[] payload, int offset, int len);

	public abstract void onWebSocketClose(int statusCode, String reason);

	public abstract void onWebSocketConnect();

	public abstract void onWebSocketError(Throwable cause);

	public abstract void onWebSocketText(String message);

	public void send(String str) {
		try {
			client.sendString(str);
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public void sendJSON(String key, Object obj){
		try {
			Gson gson = new Gson();
			JsonElement value = gson.toJsonTree(obj);
			JsonObject message = new JsonObject();
			message.addProperty("key", key);
			message.add("value", value);			
			String text = gson.toJson(message);
			client.sendString(text);
		} catch ( IOException ex){
			ex.printStackTrace();
		}
	}

	// TODO: Change it to setSession something
	public void setSession(Session session) {
		this.session = session;
		this.client = session.getRemote();
	}

}
