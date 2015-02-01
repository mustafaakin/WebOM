package webom.core;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.UpgradeRequest;
import org.eclipse.jetty.websocket.api.UpgradeResponse;
import org.eclipse.jetty.websocket.api.WebSocketListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.annotations.request.RequestMethod;
import webom.request.WebSocketHandler;
import webom.session.SessionBackend;

public class MainWebSocketHandler implements WebSocketListener {
	private static final Logger logger = LoggerFactory.getLogger(MainWebSocketHandler.class);

	Session session;
	WebSocketHandler handler;
	WebOM webom;

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
		if (webom == null) {
			session.close(1002, "Something is wrong, w is null, like you will understand");
			return;
		}

		this.session = session;

		UpgradeRequest req = session.getUpgradeRequest();
		UpgradeResponse res = session.getUpgradeResponse();

		// MainHTTPHandler.SESSION_HEADER_NAME
		String path = req.getRequestURI().getPath();
		Route route = webom.findMatchingRoute(path, RequestMethod.WEBSOCKET.str);
		if (route == null) {
			session.close(1002, "The request websocket path is not found");
		} else {
			Class<?> routeCls = route.requestHandlerCls;
			try {
				handler = (WebSocketHandler) routeCls.newInstance();
				handler.setSession(session);

				List<HttpCookie> cookies = req.getCookies();
				HttpCookie foundCookie = null;
				String sessionKey = null;
				if (cookies != null) {
					for (HttpCookie cookie : cookies) {
						if (cookie.getName().equals(MainHTTPHandler.SESSION_HEADER_NAME)) {
							sessionKey = cookie.getValue();
							foundCookie = cookie;
							break;
						}
					}
				}

				// Found session key!!
				SessionBackend sessionBackend = webom.getSessionBackend();
				webom.session.Session sess = null;
				// TODO: Make it more beautiful, remove duplicated code
				// In Websocket, ignore non cookied ones, do not create a cookie
				// here.
				if (sessionKey != null) {
					sess = sessionBackend.get(sessionKey);
				}

				Map<String, String> urlParams = route.matches(path);
				Map<String, List<String>> queryMap = req.getParameterMap();

				// TODO: Also Get the session params
				logger.info("Websocket request for {} will be handled by {}", path, routeCls);
				handler.buildWebsocket(urlParams, queryMap, sess);

				// TODO: NULL CHECK!

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
		this.webom = w;
	}

}
