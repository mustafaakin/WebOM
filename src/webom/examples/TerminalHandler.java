package webom.examples;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.annotations.validation.NotNull;
import webom.annotations.validation.Param;
import webom.request.WebSocketHandler;

@Handler(method = RequestMethod.WEBSOCKET, path = "/terminal/:termId")
public class TerminalHandler extends WebSocketHandler {
		
	private static Logger logger = LoggerFactory.getLogger(TerminalHandler.class);

	@NotNull
	@Param
	public String termId;

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {

	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		logger.info("Web socket closed, code: {} reason: {}", statusCode, reason);
	}

	@Override
	public void onWebSocketConnect() {
		logger.info("Websocket connected");
		send("Hello, it seems you want the terminal: " + termId);
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace();
	}

	@Override
	public void onWebSocketText(String message) {
		logger.info("Recieved ws message: {}", message);
	}

}
