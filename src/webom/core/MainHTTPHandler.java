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
import webom.response.transform.FileTransformer;
import webom.response.transform.StaticFileHandler;
import webom.response.transform.StringTransformer;
import webom.session.Session;
import webom.session.SessionBackend;
import webom.util.HTTPStatus;

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
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
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

					logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, res.getRaw().getStatus(),
							String.format("%.2f", diff));
					baseRequest.setHandled(true);
					return;
				}
			}

			// HTTP Cross origin, cors, csrf etc
			Route route = webom.findMatchingRoute(target, baseRequest.getMethod());
			if (route == null) {
				response.setStatus(HTTPStatus.NOT_FOUND);
				response.getWriter().println("Content not found, sorry.");
				long endTime = System.nanoTime();
				double diff = (endTime / 1000000.0 - startTime / 1000000.0);
				logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, HTTPStatus.NOT_FOUND,
						String.format("%.2f", diff));
			} else {
				// Find out the class of the handler which should be a request
				// handler
				Class<?> routeCls = route.requestHandlerCls;

				// Get the parameters in URL
				Map<String, String> urlParams = route.matches(target);

				try {
					// TODO: Also build the session!!
					Cookie[] cookies = request.getCookies();
					String sessionKey = null;
					if (cookies != null) {
						for (Cookie cookie : cookies) {
							if (cookie.getName().equals(SESSION_HEADER_NAME)) {
								sessionKey = cookie.getValue();
								break;
							}
						}
					}

					SessionBackend sessionBackend = webom.getSessionBackend();
					Session session = sessionBackend.get(sessionKey);

					if (session == null) {
						session = new Session(sessionBackend);
						sessionKey = session.getKey();
						Cookie cookie = new Cookie(SESSION_HEADER_NAME, sessionKey);
						response.addCookie(cookie);
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
					instance.buildHTTP(urlParams, req.getRaw().getParameterMap(), session);

					// Determine if the requirements are satisfied
					boolean isValid = instance.isValid();
					if (!isValid) {
						res.getRaw().getWriter().println("The request is invalid:" + instance.getValidation_messages());
						res.getRaw().setStatus(HTTPStatus.BAD_REQUEST);
						// Don't attempt to handle the request
					} else {
						// Call the handle method of the newly instantiated
						// class
						Object result = instance.handle(req, res);

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

						} else if (result instanceof Object) {
							// WTF Then?
						} else {
							// Actually can never be here
						}
					}

					// If it is not modified, it is up to SessionBackend to
					// decide
					// if actually update or leave it as it is
					session.save();

					long endTime = System.nanoTime();
					double diff = (endTime / 1000000.0 - startTime / 1000000.0);
					logger.info("{} {} {} {} ms", baseRequest.getMethod(), target, res.getRaw().getStatus(),
							String.format("%.2f", diff));
				} catch (Exception e) {
					// Ignore for now
				}
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

			long endTime = System.nanoTime();
			double diff = (endTime / 1000000.0 - startTime / 1000000.0);
			// Better error reporting
			logger.info("{} {} {} {} ms: {}", baseRequest.getMethod(), target, HTTPStatus.INTERNAL_SERVER_ERROR,
					String.format("%.2f", diff, ex.getMessage()));
			baseRequest.setHandled(true);
		}
	}
}
