package webom.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.annotations.request.Handler;
import webom.annotations.request.RequestMethod;
import webom.response.transform.StaticFileHandler;
import webom.session.InMemorySession;
import webom.session.SessionBackend;

public class WebOM {
	private static Logger logger = LoggerFactory.getLogger(WebOM.class);
	private boolean productionMode = false;

	HashMap<String, HashMap<Integer, ArrayList<Route>>> routes = new HashMap<>();

	private int port;

	private StaticFileHandler staticFileHandler;

	private SessionBackend session = new InMemorySession();

	public WebOM(int port, String packageName) {
		this.port = port;

		Reflections reflections = new Reflections(packageName);
		Set<Class<?>> annotated = reflections.getTypesAnnotatedWith(Handler.class);

		for (Class<?> cls : annotated) {
			Handler handler = cls.getAnnotation(Handler.class);
			RequestMethod method = handler.method();
			String path = handler.path();
			this.addRoute(path, method.str, cls);
		}
	}

	public void addRoute(String path, String method, Class<?> cls) {
		logger.info("Mapping route {} {}, ({})", method, path, cls.getName());
		String[] pathComponents = Route.getComponentsFromPath(path);

		// Look for HTTP Method; GET, POST, etc..
		if (!routes.containsKey(method)) {
			routes.put(method, new HashMap<>());
		}
		HashMap<Integer, ArrayList<Route>> methodRoutes = routes.get(method);

		if (!methodRoutes.containsKey(pathComponents.length)) {
			methodRoutes.put(pathComponents.length, new ArrayList<Route>());
		}
		// Then look how many components does the path have
		ArrayList<Route> lengthRoutes = methodRoutes.get(pathComponents.length);

		Route route = new Route(path, cls);
		lengthRoutes.add(route);
	}

	public Route findMatchingRoute(String path, String method) {
		String[] pathComponents = Route.getComponentsFromPath(path);

		// Look for HTTP Method; GET, POST, etc..
		HashMap<Integer, ArrayList<Route>> methodRoutes = routes.get(method);
		if (methodRoutes == null)
			return null;

		// Then look how many components does the path have
		ArrayList<Route> lengthRoutes = methodRoutes.get(pathComponents.length);
		if (lengthRoutes == null)
			return null;

		// Search which route matches the given method
		Route matchedRoute = null;
		for (Route route : lengthRoutes) {
			Map<String, String> variables = route.matches(path);
			if (variables != null) {
				matchedRoute = route;
				break;
			}
		}
		return matchedRoute;
	}

	public SessionBackend getSessionBackend() {
		return session;
	}

	public StaticFileHandler getStaticFileHandler() {
		return staticFileHandler;
	}

	public boolean isProductionMode() {
		return productionMode;
	}

	public void setProductionMode(boolean productionMode) {
		this.productionMode = productionMode;
	}

	public void setSession(SessionBackend session) {
		this.session = session;
	}

	public void setStaticFileLocation(String path) {
		this.staticFileHandler = new StaticFileHandler(path);
	}

	public void start() {
		HandlerCollection handlerCollection = new HandlerCollection();

		// Initialize Main HTTP handler that will respond to anything
		MainHTTPHandler http = new MainHTTPHandler(this);

		handlerCollection.addHandler(http);

		final WebOM w = this;
		// Initialize Main Web Socket handler that will respond to anything
		WebSocketHandler wsHandler = new WebSocketHandler() {
			@Override
			public void configure(WebSocketServletFactory factory) {
				factory.register(MainWebSocketHandler.class);

				final WebSocketCreator creator = factory.getCreator();
				factory.setCreator(new WebSocketCreator() {
					@Override
					public Object createWebSocket(ServletUpgradeRequest req, ServletUpgradeResponse resp) {
						Object webSocket = creator.createWebSocket(req, resp);
						if (webSocket instanceof MainWebSocketHandler) {
							MainWebSocketHandler mwsh = (MainWebSocketHandler) webSocket;
							mwsh.setWebom(w);
						}
						return webSocket;
					}
				});
			}

		};
		handlerCollection.addHandler(wsHandler);

		try {
			HttpConfiguration httpConfig = new HttpConfiguration();
			httpConfig.setSendServerVersion(false);
			HttpConnectionFactory httpFactory = new HttpConnectionFactory(httpConfig);

			Server server = new Server();

			ServerConnector httpConnector = new ServerConnector(server, httpFactory);
			httpConnector.setPort(port);

			server.setConnectors(new Connector[] { httpConnector });
			server.setHandler(handlerCollection);
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void stop() {
		// TODO:
	}
}
