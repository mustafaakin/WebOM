package webom.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.annotations.request.WebSocketKey;
import webom.request.KeyValueWebSocketHandler;

@Handler(method = RequestMethod.WEBSOCKET, path = "/keyvalue")
public class KeyValueWSExample extends KeyValueWebSocketHandler{

	private static Logger logger = LoggerFactory.getLogger(KeyValueWSExample.class);
 

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		logger.info("Web socket closed, code: {} reason: {}", statusCode, reason);
	}

	@Override
	public void onWebSocketConnect() {
		logger.info("Websocket connected");
		super.onWebSocketConnect();
		Person p = new Person();
		p.age = 24;
		p.name = "Mustafa";
		sendJSON("myperson", p);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace();
	}

	@Override
	public void onUnknownWebSocketText(String message) {
		logger.info("Unknown message recieved");
	}
	
	public static class Person {
		int age;
		String name;
		@Override
		public String toString() {
			return "Hi i am " + name + " and " + age + " years old";
		}
	}
	
	@WebSocketKey(key="person")
	public void onPerson(Person person){
		logger.info("Person recieved and it says " + person);
	}
	
}
