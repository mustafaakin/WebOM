package webom.core;

import java.util.List;
import java.util.Map;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;

import webom.annotations.request.RequestMethod;
import webom.request.WebSocketHandler;

public class MainWebSocketHandler implements WebSocketListener {
	Session session;
	WebSocketHandler handler;
	WebOM w;

	public MainWebSocketHandler() {

	}

	@Override
	public void onWebSocketBinary(byte[] payload, int offset, int len) {
		if (handler != null) {
			handler.onWebSocketBinary(payload, offset, len);
		}
	}

	@Override
	public void onWebSocketClose(int statusCode, String reason) {
		if (handler != null) {
			handler.onWebSocketClose(statusCode, reason);
		}
	}

	@Override
	public void onWebSocketConnect(Session session) {
		if (w == null) {
			session.close(1002, "Something is wrong, w is null, like you will understand");
			return;
		}

		this.session = session;

		UpgradeRequest req = session.getUpgradeRequest();
		UpgradeResponse res = session.getUpgradeResponse();

		// MainHTTPHandler.SESSION_HEADER_NAME
		String path = req.getRequestURI().getPath();
		Route route = w.findMatchingRoute(path, RequestMethod.WEBSOCKET.str);
		if (route == null) {
			session.close(1002, "The request websocket path is not found");
		} else {
			Class<?> routeCls = route.requestHandlerCls;
			try {
				handler = (WebSocketHandler) routeCls.newInstance();
				handler.setSession(session);

				Map<String, String> urlParams = route.matches(path);
				Map<String, List<String>> queryMap = req.getParameterMap();

				// TODO: Also Get the session params
				handler.buildWebsocket(urlParams, queryMap, null);

				handler.onWebSocketConnect();
			} catch (Exception ex) {
				ex.printStackTrace();
				session.close(1001, "An internal error has occured");
			}
		}
	}

	@Override
	public void onWebSocketError(Throwable cause) {
		if (handler != null) {
			handler.onWebSocketError(cause);
		}
	}

	@Override
	public void onWebSocketText(String message) {
		if (handler != null) {
			handler.onWebSocketText(message);
		}
	}

	public void setWebom(WebOM w) {
		this.w = w;
	}

}
