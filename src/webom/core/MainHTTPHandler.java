package webom.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import webom.request.HTTPRequestHandler;
import webom.response.Message;
import webom.response.transform.FileTransformer;
import webom.response.transform.StaticFileHandler;
import webom.response.transform.StringTransformer;
import webom.session.Session;
import webom.session.SessionBackend;
import webom.util.ContentType;
import webom.util.HTTPStatus;

import com.google.gson.Gson;

public class MainHTTPHandler extends AbstractHandler {
	private static final Logger logger = LoggerFactory.getLogger(MainHTTPHandler.class);
	private WebOM webom;

	// TODO: Anyone has a better idea than this, instead of writing singletons
	// to each?
	// Cannot write to interface, maybe make interface abstract class?
	// Transformers
	private static final StringTransformer stringTransformer = new StringTransformer();
	private static final FileTransformer fileTransformer = new FileTransformer();

	public static final String SESSION_HEADER_NAME = "X-MySession";

	public MainHTTPHandler(WebOM webom) {
		this.webom = webom;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		long startTime = System.nanoTime();
		try {
			// Serve the static file if it exists, Session-less
			StaticFileHandler sf = webom.getStaticFileHandler();
			if (sf != null) {
				File f = sf.getFile(target);
				if (f.isFile() && f.exists()) {
					webom.request.Request req = new webom.request.Request(request, null);
					webom.response.Response res = new webom.response.Response(response);

					fileTransformer.transform(req, res, f);
					long endTime = System.nanoTime();
					double diff = (endTime / 1000000.0 - startTime / 1000000.0);

					logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, res.getRaw().getStatus(), String.format("%.2f", diff));
					baseRequest.setHandled(true);
					return;
				}
			}

			// HTTP Cross origin, cors, csrf etc
			Route route = webom.findMatchingRoute(target, baseRequest.getMethod());
			if (route == null) {
				// Try to find a Websocket for that, we must hand over the
				// connection to there, there should not be a 404
				if (webom.findMatchingRoute(target, "WEBSOCKET") != null) {
					return;
				}
				response.setStatus(HTTPStatus.NOT_FOUND);
				response.getWriter().println("Content not found, sorry.");
				long endTime = System.nanoTime();
				double diff = (endTime / 1000000.0 - startTime / 1000000.0);
				logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, HTTPStatus.NOT_FOUND, String.format("%.2f", diff));
			} else {
				// Find out the class of the handler which should be a request
				// handler
				Class<?> routeCls = route.requestHandlerCls;

				// Get the parameters in URL
				Map<String, String> urlParams = route.matches(target);

				// TODO: Also build the session!!
				Cookie[] cookies = request.getCookies();
				Cookie foundCookie = null;
				String sessionKey = null;
				if (cookies != null) {
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals(SESSION_HEADER_NAME)) {
							sessionKey = cookie.getValue();
							foundCookie = cookie;
							break;
						}
					}
				}

				SessionBackend sessionBackend = webom.getSessionBackend();
				Session session;

				// TODO: Make it more beautiful, remove duplicated code
				if (sessionKey == null) {
					session = new Session(sessionBackend);
					sessionKey = session.getKey();
					Cookie cookie = new Cookie(SESSION_HEADER_NAME, sessionKey);
					response.addCookie(cookie);
					// logger.info("Ssseion key not found, creating new one {} ",
					// sessionKey);
				} else {
					// logger.info("Ssseion key found {}", sessionKey);
					session = sessionBackend.get(sessionKey);
					if (session == null) {
						session = new Session(sessionBackend);
						sessionKey = session.getKey();
						Cookie cookie = new Cookie(SESSION_HEADER_NAME, sessionKey);
						foundCookie.setMaxAge(0);
						response.addCookie(cookie);
						// logger.info("Ssseion id could not be found adding new key {}",
						// sessionKey);
					}
				}

				// Instantiate the given class and build the object to
				// handle it
				HTTPRequestHandler instance = (HTTPRequestHandler) routeCls.newInstance();

				// Generate our project specific Request/Response objects
				webom.request.Request req = new webom.request.Request(request, session);
				webom.response.Response res = new webom.response.Response(response);

				// Build the POJO Class field values from either URL
				// parameters,
				// or GET/POST variables
				instance.buildHTTP(urlParams, req.getRaw().getParameterMap(), session, request.getInputStream());

				// Determine if the requirements are satisfied
				boolean isValid = instance.isValid();

				if (!isValid) {
					res.getRaw().getWriter().println("The request is invalid:" + instance.getValidation_messages());
					res.getRaw().setStatus(HTTPStatus.BAD_REQUEST);
					// Don't attempt to handle the request
				} else {
					// Call the before;
					Object beforeObj = instance.before();
					// Call the handle method of the newly instantiated
					// class
					Object result;
					if (beforeObj != null) {
						result = beforeObj;
					} else {
						result = instance.handle(req, res);
					}

					// Set the content type from given Response object,
					// since
					// the handle method could have changed them
					response.setContentType(res.getContentType());
					response.setStatus(res.getStatus());

					// TODO: WHAT ABOUT CONTENT LENGTH? What is crucial?

					// TODO: Gzip on large things

					// Do the transmissions based on the returned object
					// type
					if (result instanceof String) {
						stringTransformer.transform(req, res, (String) result);
					} else if (result instanceof File) {
						fileTransformer.transform(req, res, (File) result);
					} else if (result instanceof InputStream) {

					} else if (result instanceof Message) {
						Message message = (Message) result;
						response.setContentType(ContentType.JSON);
						response.setStatus(message.getCode());

						Gson gson = new Gson();
						String jsonStr = gson.toJson(result);
						stringTransformer.transform(req, res, jsonStr);

					} else if (result instanceof Object) {
						Gson gson = new Gson();
						String jsonStr = gson.toJson(result);
						stringTransformer.transform(req, res, jsonStr);
					} else {
						// Actually can never be here
					}
				}

				// If it is not modified don't try to save it back
				if (session.isDirty()) {
					session.save();
				}

				long endTime = System.nanoTime();
				double diff = (endTime / 1000000.0 - startTime / 1000000.0);
				logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, res.getRaw().getStatus(), String.format("%.2f", diff));

				Reporter.path(target, route.path, routeCls.getName(), diff, response.getStatus());
			}
			baseRequest.setHandled(true);
		} catch (Exception ex) {
			response.setStatus(HTTPStatus.INTERNAL_SERVER_ERROR);
			PrintWriter output = response.getWriter();
			// TODO: Allow custom handlers for displaying nice things
			if (webom.isProductionMode()) {
				output.println("An internal error has occured");
			} else {
				output.println("An exception has occurred: \n");
				ex.printStackTrace(output);
				output.println("\n\n ** You can disable this message by setting the WebOM in production mode.");
			}

			ex.printStackTrace();

			long endTime = System.nanoTime();
			double diff = (endTime / 1000000.0 - startTime / 1000000.0);
			// Better error reporting
			logger.info("{} {} {} {} ms #Error: {}", baseRequest.getMethod(), target, HTTPStatus.INTERNAL_SERVER_ERROR, String.format("%.2f", diff), ex.getMessage());
			baseRequest.setHandled(true);
		}
	}
}
