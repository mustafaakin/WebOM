package webom.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

import webom.request.HTTPRequestHandler;
import webom.response.transform.FileTransformer;
import webom.response.transform.StaticFileHandler;
import webom.response.transform.StringTransformer;
import webom.session.Session;
import webom.util.HTTPStatus;

public class MainHTTPHandler extends AbstractHandler {
	private WebOM w;

	// TODO: Anyone has a better idea than this, instead of writing singletons
	// to each?
	// Cannot write to interface, maybe make interface abstract class?
	// Transformers
	private static final StringTransformer stringTransformer = new StringTransformer();
	private static final FileTransformer fileTransformer = new FileTransformer();

	public static final String SESSION_HEADER_NAME = "X-MySession";

	public MainHTTPHandler(WebOM w) {
		this.w = w;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		// Serve the static file if it exists, Session-less
		StaticFileHandler sf = w.getStaticFileHandler();
		if (sf != null) {
			File f = sf.getFile(target);
			if (f.isFile() && f.exists()) {
				webom.request.Request req = new webom.request.Request(request, null);
				webom.response.Response res = new webom.response.Response(response);

				fileTransformer.transform(req, res, f);
				baseRequest.setHandled(true);
				return;
			}
		}
		
		// HTTP Cross origin, cors, csrf etc
		
		Route route = w.findMatchingRoute(target, baseRequest.getMethod());
		if (route == null) {
			response.setStatus(HTTPStatus.NOT_FOUND);
		} else {
			// Find out the class of the handler which should be a request
			// handler
			Class<?> routeCls = route.requestHandlerCls;
			System.out.printf("%s ==> %s (%s) \n", target, route.path, routeCls);

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

				Session session = w.getSession().get(sessionKey);
				if (session == null) {
					session = new Session();
					sessionKey = session.getKey();
					Cookie cookie = new Cookie(SESSION_HEADER_NAME, sessionKey);
					response.addCookie(cookie);
				}

				// Instantiate the given class and build the object to handle it
				HTTPRequestHandler instance = (HTTPRequestHandler) routeCls.newInstance();

				// Generate our project specific Request/Response objects
				webom.request.Request req = new webom.request.Request(request, session);
				webom.response.Response res = new webom.response.Response(response);

				// Build the POJO Class field values from either URL parameters,
				// or GET/POST variables
				instance.buildHTTP(urlParams, req.getRaw().getParameterMap(), session);

				// Determine if the requirements are satisfied
				boolean isValid = instance.isValid();
				if (!isValid) {
					res.getRaw().getWriter().println("The request is invalid:" + instance.getValidation_messages());
					res.getRaw().setStatus(HTTPStatus.BAD_REQUEST);
					// Don't attempt to handle the request
				} else {
					// Call the handle method of the newly instantiated class
					Object result = instance.handle(req, res);

					// Set the content type from given Response object, since
					// the handle method could have changed them
					response.setContentType(res.getContentType());
					response.setStatus(res.getStatus());

					// TODO: WHAT ABOUT CONTENT LENGTH? What is crucial?

					// TODO: Gzip on large things
					
					// Do the transmissions based on the returned object type
					if (result instanceof String) {						
						stringTransformer.transform(req, res, (String) result);
					} else if (result instanceof File) {
						// Do magic
					} else if (result instanceof InputStream) {

					} else if (result instanceof Object) {
						// WTF Then?
					} else {
						// Actually can never be here
					}
				}

				w.getSession().set(session);
			} catch (Exception e) {
				e.printStackTrace();
			}
			baseRequest.setHandled(true);
		}

	}
}
