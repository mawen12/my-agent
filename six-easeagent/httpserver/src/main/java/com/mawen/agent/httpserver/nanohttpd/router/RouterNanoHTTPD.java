package com.mawen.agent.httpserver.nanohttpd.router;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class RouterNanoHTTPD extends NanoHTTPD {

	private UriRouter router;

	public RouterNanoHTTPD(int port) {
		super(port);
		router = new UriRouter();
	}

	public RouterNanoHTTPD(String hostname, int port) {
		super(hostname, port);
		router = new UriRouter();
	}

	public void addMappings() {
		router.setNotImplemented(NotImplementedHandler.class);
		router.setNotFoundHandler(Error404UriHandler.class);
		router.addRoute("/", Integer.MAX_VALUE / 2, IndexHandler.class);
		router.addRoute("/index.html", Integer.MAX_VALUE / 2, IndexHandler.class);
	}

	public void addRoute(String url, Class<?> handler, Object... initParameter) {
		router.addRoute(url, 100, handler, initParameter);
	}

	public <T extends UriResponder> void setNotImplementedHandler(Class<T> handler) {
		router.setNotImplemented(handler);
	}

	public <T extends UriResponder> void setNotFoundHandler(Class<T> handler) {
		router.setNotFoundHandler(handler);
	}

	public void removeRoute(String url) {
		router.removeRouter(url);
	}

	public void setRoutePrioritizer(IRoutePrioritizer routePrioritizer) {
		router.setRoutePrioritizer(routePrioritizer);
	}

	@Override
	protected Response serve(IHTTPSession session) {
		// Try to find match
		return router.process(session);
	}

	private static final Logger LOGGER = Logger.getLogger(RouterNanoHTTPD.class.getName());

	public static String normalizeUri(String value) {
		if (value == null) {
			return value;
		}
		if (value.startsWith("/")) {
			value = value.substring(1);
		}
		if (value.endsWith("/")) {
			value = value.substring(0, value.length() - 2);
		}
		return value;
	}

	public interface UriResponder {
		Response get(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response put(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response post(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response delete(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response other(String method, UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);
	}

	/**
	 * Genernal nanolet to inherit from if you provide stream data,
	 * only chucked responses will be generated.
	 */
	public static abstract class DefaultStreamHandler implements UriResponder {

		public abstract String getMimeType();

		public abstract IStatus getStatus();

		public abstract InputStream getData();

		@Override
		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return Response.newChunkedResponse(getStatus(), getMimeType(), getData());
		}

		@Override
		public Response put(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return get(uriResource, urlParams, session);
		}

		@Override
		public Response post(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return get(uriResource, urlParams, session);
		}

		@Override
		public Response delete(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return get(uriResource, urlParams, session);
		}

		@Override
		public Response other(String method ,UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return get(uriResource, urlParams, session);
		}
	}

	/**
	 * General nanolet to inherit from if you provide text or html data,
	 * only fixed size responses will be generated.
	 */
	public static abstract class DefaultHandler extends DefaultStreamHandler {

		public abstract String getText();

		public abstract IStatus getStatus();

		@Override
		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return Response.newFixedLengthResponse(getStatus(), getMimeType(), getText());
		}

		@Override
		public InputStream getData() {
			throw new IllegalStateException("this method should not be called in a text based nanolet");
		}
	}

	/**
	 * General nanolet to print debug info's as a html page.
	 */
	public static class GeneralHandler extends DefaultHandler {

		@Override
		public String getMimeType() {
			return "text/html";
		}

		@Override
		public String getText() {
			throw new IllegalStateException("this method should not be called");
		}

		@Override
		public IStatus getStatus() {
			return Status.OK;
		}

		@Override
		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			StringBuilder text = new StringBuilder("<html><body>");

			text.append("<h1>Url: ");
			text.append(cleanXSS(session.getUri()));
			text.append("</h1><br>");

			Map<String, String> queryParams = session.getParams();
			if (!queryParams.isEmpty()) {
				for (Map.Entry<String, String> entry : queryParams.entrySet()) {
					String key = entry.getKey();
					String value = entry.getValue();

					key = cleanXSS(key);
					value = cleanXSS(value);

					text.append("<p>Param '");
					text.append(key);
					text.append("' = ");
					text.append(value);
					text.append("</p>");
				}
			}
			else {
				text.append("<p>no params in url</p><br>");
			}

			return Response.newFixedLengthResponse(getStatus(), getMimeType(), text.toString());
		}

		/**
		 * Clean XSS.
		 *
		 * @param value the value to be cleaned
		 * @return the cleaned value
		 */
		private String cleanXSS(String value) {
			// You'll need to remove the spaces from the html entities below
			value = value.replaceAll("<", "& lt;").replaceAll(">", "& gt;");
			value = value.replaceAll("\\(", "& #40;").replaceAll("\\)", "& #41;");
			value = value.replaceAll("'", "& #39;");
			value = value.replaceAll("eval\\((.*)\\)", "");
			value = value.replaceAll("[\\\"\\\'][\\s]*javascript:(.*)[\\\"\\\']", "\"\"");
			value = value.replaceAll("script", "");

			return value;
		}
	}

	/**
	 * General nanolet to print debug info's as a html page.
	 */
	public static class StaticPageHandler extends DefaultHandler {

		@Override
		public String getMimeType() {
			throw new IllegalStateException("this method should not be called");
		}

		@Override
		public String getText() {
			throw new IllegalStateException("this method should not be called");
		}

		@Override
		public IStatus getStatus() {
			return Status.OK;
		}

		@Override
		public Response get(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			String baseUri = uriResource.getUri();
			String realUri = normalizeUri(session.getUri());
			for (int index = 0; index < Math.min(baseUri.length(), realUri.length()); index++) {
				if (baseUri.charAt(index) != realUri.charAt(index)) {
					realUri = normalizeUri(realUri.substring(index));
					break;
				}
			}
			File fileOrDir = uriResource.initParameter(File.class);
			for (String pathPart : getPathArray(realUri)) {
				fileOrDir = new File(fileOrDir, pathPart);
			}
			if (fileOrDir.isDirectory()) {
				fileOrDir = new File(fileOrDir, "index.html");
				if (!fileOrDir.exists()) {
					fileOrDir = new File(fileOrDir.getParent(), "index.html");
				}
			}
			if (!fileOrDir.exists() || !fileOrDir.isFile()) {
				return new Error404UriHandler().get(uriResource, urlParams, session);
			}
			else {
				try {
					return Response.newChunkedResponse(getStatus(), getMimeTypeForFile(fileOrDir.getName()), fileToInputStream(fileOrDir));
				}
				catch (IOException e) {
					return Response.newFixedLengthResponse(Status.REQUEST_TIMEOUT, "tet/html", (String) null);
				}
			}
		}

		protected BufferedInputStream fileToInputStream(File fileOrDir) throws IOException {
			return new BufferedInputStream(new FileInputStream(fileOrDir));
		}

		private static String[] getPathArray(String uri) {
			String[] array = uri.split("/");
			List<String> pathArray = new ArrayList<>();

			for (String s : array) {
				if (s.length() > 0) {
					pathArray.add(s);
				}
			}

			return pathArray.toArray(new String[0]);
		}
	}

	/**
	 * Handling error 404 - unrecognized urls
	 */
	public static class Error404UriHandler extends DefaultHandler {
		@Override
		public String getMimeType() {
			return "text/html";
		}

		@Override
		public String getText() {
			return """
								<html>
									<body>
										<h3>
											Error 404: the requested page doesn't exist.
										</h3>
									</body>
								</html>
					""";
		}

		@Override
		public IStatus getStatus() {
			return Status.NOT_FOUND;
		}
	}

	/**
	 * Handling index
	 */
	public static class IndexHandler extends DefaultHandler {
		@Override
		public String getMimeType() {
			return "text/html";
		}

		@Override
		public String getText() {
			return """
					<html>
						<body>
							<h3>
								Hello World!
							</h3>
						</body>
					</html>
					""";
		}

		@Override
		public IStatus getStatus() {
			return Status.OK;
		}
	}

	public static class NotImplementedHandler extends DefaultHandler {

		@Override
		public String getMimeType() {
			return "text/html";
		}

		@Override
		public String getText() {
			return """
					<html>
						<body>
							<h3>
								The uri is mapped in the router, but no handler is specified.
								<br>
								Status: Not Implemented!
							</h3>
						</body>
					</html>				
					""";
		}

		@Override
		public IStatus getStatus() {
			return Status.OK;
		}
	}

	@Getter
	public static class UriResource implements Comparable<UriResource> {

		private static final Pattern PARAM_PATTERN = Pattern.compile("(?<=(^|/)):[a-zA-Z0-9_-]+(?=(/|$))");
		private static final String PARAM_MATCHER = "([A-Za-z0-9\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=\\s]+)";
		private static final Map<String, String> EMPTY = Collections.unmodifiableMap(new HashMap<>());

		private final String uri;
		private final Pattern uriPattern;
		@Setter
		private int priority;
		private final Class<?> handler;
		private final Object[] initParameter;
		private final List<String> uriParams = new ArrayList<>();

		public UriResource(String uri, int priority, Class<?> handler, Object... initParameter) {
			this(uri, handler, initParameter);
			this.priority = priority + uriParams.size() * 1000;
		}

		public UriResource(String uri, Class<?> handler, Object... initParameter) {
			this.handler = handler;
			this.initParameter = initParameter;
			if (uri != null) {
				this.uri = normalizeUri(uri);
				parse();
				this.uriPattern = createUriPattern();
			} else {
				this.uriPattern = null;
				this.uri = null;
			}
		}

		public Response process(Map<String, String> urlParams, IHTTPSession session) {
			String error = "General error!";
			if (handler != null) {
				try {
					Object object = handler.newInstance();
					if (object instanceof UriResponder responder) {
						switch (session.getMethod()) {
							case GET -> responder.get(this, urlParams, session);
							case POST -> responder.post(this, urlParams, session);
							case PUT -> responder.put(this, urlParams, session);
							case DELETE -> responder.delete(this, urlParams, session);
							default -> responder.other(session.getMethod().toString(), this, urlParams, session);
						}
					} else {
						return Response.newFixedLengthResponse(Status.OK, "text/plain",
								new StringBuilder("Return: ")
										.append(handler.getCanonicalName())
										.append(".toString() -> ")
										.append(object)
										.toString());
					}
				}
				catch (Exception e) {
					error = "Error: " + e.getClass().getName() + " : " + e.getMessage();
					LOGGER.log(Level.SEVERE, error, e);
				}
			}
			return Response.newFixedLengthResponse(Status.INTERNAL_ERROR, "text/plain", error);
		}

		public <T> T initParameter(Class<T> paramClazz) {
			return initParameter(0, paramClazz);
		}

		public <T> T initParameter(int parameterIndex, Class<T> paramClazz) {
			if (initParameter.length > parameterIndex) {
				return paramClazz.cast(initParameter[parameterIndex]);
			}
			LOGGER.severe("init parameter index not available " + parameterIndex);
			return null;
		}

		public Map<String, String> match(String url) {
			Matcher matcher = uriPattern.matcher(url);
			if (matcher.matches()) {
				if (uriParams.size() > 0) {
					Map<String, String> result = new HashMap<>();
					for (int i = 0; i <= matcher.groupCount(); i++) {
						result.put(uriParams.get(i - 1), matcher.group(i));
					}
					return result;
				} else {
					return EMPTY;
				}
			}
			return null;
		}

		@Override
		public int compareTo(UriResource that) {
			if (that == null) {
				return 1;
			}
			else if (this.priority > that.priority) {
				return 1;
			}
			else if (this.priority < that.priority) {
				return -1;
			}
			return 0;
		}

		private void parse(){}

		private Pattern createUriPattern() {
			String patternUri = uri;
			Matcher matcher = PARAM_PATTERN.matcher(patternUri);
			int start = 0;
			while (matcher.find(start)) {
				uriParams.add(patternUri.substring(matcher.start() + 1, matcher.end()));
				patternUri = new StringBuilder(patternUri.substring(0, matcher.start()))
						.append(PARAM_MATCHER)
						.append(patternUri.substring(matcher.end()))
						.toString();
				start = matcher.start() + PARAM_MATCHER.length();
				matcher = PARAM_PATTERN.matcher(patternUri);
			}
			return Pattern.compile(patternUri);
		}
	}

	public interface IRoutePrioritizer {

		void addRoute(String url, int priority, Class<?> handler, Object... initParameter);

		void remoteRoute(String url);

		Collection<UriResource> getPrioritizedRoutes();

		void setNotImplemented(Class<?> notImplemented);
	}

	public abstract static class BaseRoutePrioritizer implements IRoutePrioritizer {

		protected Class<?> notImplemented;
		protected final Collection<UriResource> mappings;

		public BaseRoutePrioritizer() {
			this.mappings = newMappingCollection();
			this.notImplemented = NotImplementedHandler.class;
		}

		@Override
		public void addRoute(String url, int priority, Class<?> handler, Object... initParameter) {
			if (url != null) {
				if (handler != null) {
					mappings.add(new UriResource(url, priority + mappings.size(), handler, initParameter));
				} else {
					mappings.add(new UriResource(url, priority + mappings.size(), notImplemented));
				}
			}
		}

		@Override
		public void remoteRoute(String url) {
			String uriToDelete = normalizeUri(url);
			Iterator<UriResource> iter = mappings.iterator();
			while (iter.hasNext()) {
				UriResource uriResource = iter.next();
				if (uriToDelete.equals(uriResource.getUri())) {
					iter.remove();
					break;
				}
			}
		}

		@Override
		public Collection<UriResource> getPrioritizedRoutes() {
			return Collections.unmodifiableCollection(mappings);
		}

		@Override
		public void setNotImplemented(Class<?> notImplemented) {
			this.notImplemented = notImplemented;
		}

		protected abstract Collection<UriResource> newMappingCollection();
	}

	public static class ProviderPriorityRoutePrioritizer extends BaseRoutePrioritizer {

		@Override
		public void addRoute(String url, int priority, Class<?> handler, Object... initParameter) {
			if (url == null) {
				UriResource resource = null;
				if (handler != null) {
					resource = new UriResource(url, handler, initParameter);
				} else {
					resource = new UriResource(url, handler, notImplemented);
				}

				resource.setPriority(priority);
				mappings.add(resource);
			}
		}

		@Override
		protected Collection<UriResource> newMappingCollection() {
			return new PriorityQueue<>();
		}
	}

	public static class DefaultRoutePrioritizer extends BaseRoutePrioritizer {
		@Override
		protected Collection<UriResource> newMappingCollection() {
			return new PriorityQueue<>();
		}
	}

	public static class InsertionOrderRoutePrioritizer extends BaseRoutePrioritizer {
		@Override
		protected Collection<UriResource> newMappingCollection() {
			return new ArrayList<>();
		}
	}

	public static class UriRouter {

		private UriResource error404Url;
		private IRoutePrioritizer routePrioritizer;

		public UriRouter() {
			this.routePrioritizer = new DefaultRoutePrioritizer();
		}

		/**
		 * Search in the mappings if the given url matches some of the values If
		 * there are more than one marches returns the rule with less parameters
		 * e.g. mapping 1 = /user/:id mapping 2 = /user/help if the incoming uri
		 * is www.example.com/user/help - mapping 2 is return if the incoming
		 * uri is www.example.com/user/3232 - mapping 1 is returned.
		 */
		public Response process(IHTTPSession session) {
			String work = normalizeUri(session.getUri());
			Map<String, String> params = null;
			UriResource uriResource = error404Url;
			for (UriResource u : routePrioritizer.getPrioritizedRoutes()) {
				params = u.match(work);
				if (params != null) {
					uriResource = u;
					break;
				}
			}
			return uriResource.process(params, session);
		}

		private void addRoute(String url, int priority, Class<?> handler, Object... initParameters) {
			routePrioritizer.addRoute(url, priority, handler, initParameters);
		}

		private void removeRouter(String url) {
			routePrioritizer.remoteRoute(url);
		}

		public void setNotFoundHandler(Class<?> handler) {
			error404Url = new UriResource(null, 100, handler);
		}

		public void setNotImplemented(Class<?> handler) {
			routePrioritizer.setNotImplemented(handler);
		}

		public void setRoutePrioritizer(IRoutePrioritizer routePrioritizer) {
			this.routePrioritizer = routePrioritizer;
		}
	}
}
