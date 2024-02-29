package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.sockets.DefaultServerSocketFactory;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.sockets.SecureServerSocketFactory;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles.DefaultTempFileManagerFactory;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.tempfiles.ITempFileManager;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.threading.DefaultAsyncRunner;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.threading.IAsyncRunner;
import com.mawen.agent.httpserver.nanohttpd.util.IFactory;
import com.mawen.agent.httpserver.nanohttpd.util.IFactoryThrowing;
import com.mawen.agent.httpserver.nanohttpd.util.IHandler;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Setter
@Getter
public class NanoHTTPD {
	/**
	 * logger to log to.
	 */
	public static final Logger LOGGER = Logger.getLogger(NanoHTTPD.class.getName());
	public static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
	public static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);
	public static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
	public static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);
	public static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
	public static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX, Pattern.CASE_INSENSITIVE);

	/**
	 * Maximum time to wait on {@link Socket#getInputStream()} {@link InputStream#read()} (in milliseconds)
	 * This is required as the Keep-Alive HTTP connections would otherwise block
	 * the socket reading thread forever (or as long the browser is open).
	 */
	public static final int SOCKET_READ_TIMEOUT = 5000;
	/**
	 * Common MIME type for dynamic content: plain text
	 */
	public static final String MIME_PLAINTEXT = "text/plain";
	/**
	 * Common MIME type for dynamic content: html
	 */
	public static final String MIME_HTML = "text/html";
	/**
	 * Pseudo-Parameter to use to store the actual query string in the
	 * parameters map for later re-processing
	 */
	private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";
	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	protected static Map<String, String> MIME_TYPES;

	public final String hostname;
	public final int myPort;
	private volatile ServerSocket myServerSocket;
	private IFactoryThrowing<ServerSocket, IOException> serverSocketFactory = new DefaultServerSocketFactory();
	private Thread myThread;
	private IHandler<IHTTPSession, Response> httpHandler;
	private List<IHandler<IHTTPSession, Response>> interceptors = new ArrayList<>(4);

	protected IAsyncRunner asyncRunner;
	private IFactory<ITempFileManager> tempFileManagerFactory;

	public NanoHTTPD(int port) {
		this(null, port);
	}

	public NanoHTTPD(String hostname, int port) {
		this.hostname = hostname;
		this.myPort = port;
		setTempFileManagerFactory(new DefaultTempFileManagerFactory());
		setAsyncRunner(new DefaultAsyncRunner());

		// creates a default handler that redirects to deprecated serve();
		this.httpHandler = NanoHTTPD.this::serve;
	}

	public void addHTTPInterceptor(IHandler<IHTTPSession, Response> interceptor) {
		interceptors.add(interceptor);
	}

	public synchronized void closeAllConnections() {
		stop();
	}

	protected ClientHandler createClientHandler(final Socket finalAccept, final InputStream inputStream) {
		return new ClientHandler(this, inputStream, finalAccept);
	}

	/**
	 * Decode parameters from a URL, handing the case where a single parameter
	 * name might have been supplied several times, by return lists of values.
	 * In general these lists will contain a single element.
	 *
	 * @param params original <b>NanoHTTPD</b> parameters values, as passed to the {@link #serve(IHTTPSession)} method.
	 * @return a map of {@code String} (parameter name) to {@code List<String>} (a list of the values supplied).
	 */
	protected static Map<String, List<String>> decodeParameters(Map<String, String> params) {
		return decodeParameters(params.get(NanoHTTPD.QUERY_STRING_PARAMETER));
	}

	/**
	 * Decode parameters from a URL, handing the case where a single parameter
	 * name might have been supplied several times, by return lists of values.
	 * In general these lists will contain a single element.
	 *
	 * @param queryString a query string pulled from the URL.
	 * @return a map of {@code String} （parameter name) to {@code List<String>} (a list of the values supplied).
	 */
	protected static Map<String, List<String>> decodeParameters(String queryString) {
		Map<String, List<String>> params = new HashMap<>();
		if (queryString != null) {
			StringTokenizer st = new StringTokenizer(queryString, "&");
			while (st.hasMoreTokens()) {
				String e = st.nextToken();
				int sep = e.indexOf("=");
				String propertyName = sep >= 0 ? decodePercent(e.substring(0, sep)).trim() : decodePercent(e).trim();
				if (!params.containsKey(propertyName)) {
					params.put(propertyName, new ArrayList<>());
				}
				String propertyValue = sep >= 0 ? decodePercent(e.substring(sep + 1)) : null;
				if (propertyValue != null) {
					params.get(propertyName).add(propertyValue);
				}
			}
		}
		return params;
	}

	/**
	 * Decode percent encoded {@code String} values.
	 *
	 * @param str the percent encoded {@code String}
	 * @return expanded form of the input, for example "foo%20bar" becomes "foo bar"
	 */
	public static String decodePercent(String str) {
		String decoded = null;
		try {
			decoded = URLDecoder.decode(str, "UTF8");
		}
		catch (UnsupportedEncodingException e) {
			LOGGER.log(Level.WARNING, "Encoding not supported, ignored", e);
		}
		return decoded;
	}

	/**
	 * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and an
	 * array of loaded KeyManagers. These objects must property
	 * loaded/initialized by the caller.
	 */
	public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManager[] keyManagers) throws IOException {
		SSLServerSocketFactory res = null;
		try {
			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			trustManagerFactory.init(loadedKeyStore);
			SSLContext ctx = SSLContext.getInstance("TLS");
			ctx.init(keyManagers, trustManagerFactory.getTrustManagers(), null);
			res = ctx.getServerSocketFactory();
		}
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
		return res;
	}

	/**
	 * Creates an SSLSocketFactory for HTTPS. Pass a loaded KeyStore and a
	 * loaded KeyManagerFactory. These objects must properly loaded/initialized
	 * by the caller.
	 */
	public static SSLServerSocketFactory makeSSLSocketFactory(KeyStore loadedKeyStore, KeyManagerFactory loadedKeyFactory) throws IOException {
		try {
			return makeSSLSocketFactory(loadedKeyStore, loadedKeyFactory.getKeyManagers());
		}
		catch (Exception e) {
			throw new IOException(e.getMessage());
		}
	}

	public final int getListeningPort() {
		return this.myServerSocket == null ? -1 : this.myServerSocket.getLocalPort();
	}

	public final boolean isAlive() {
		return wasStarted() && !this.myServerSocket.isClosed() && this.myThread.isAlive();
	}

	/**
	 * Call before start() to serve over HTTPS instead of HTTP
	 */
	public void makeSecure(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
		this.serverSocketFactory = new SecureServerSocketFactory(sslServerSocketFactory, sslProtocols);
	}

	/**
	 * This is the "master" method that delegates requests to handlers and makes
	 * sure there is a response to every request.
	 * You are not supported to call or override this method in any circumstances.
	 * But no one will stop you if you do. I'm a Javadoc, not code Police.
	 *
	 * @param session the incoming session
	 * @return a response to the incoming session
	 */
	public Response handle(IHTTPSession session) {
		for (IHandler<IHTTPSession, Response> interceptor : interceptors) {
			Response response = interceptor.handle(session);
			if (response != null) {
				return response;
			}
		}
		return httpHandler.handle(session);
	}

	public static Map<String, String> mimeTypes() {
		if (MIME_TYPES == null) {
			MIME_TYPES = new HashMap<>();
			loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/default-mimetypes.properties");
			loadMimeTypes(MIME_TYPES, "META-INF/nanohttpd/mimetypes.properties");
			if (MIME_TYPES.isEmpty()) {
				LOGGER.log(Level.WARNING, "no mime types found in the classpath! please provide mimetypes.properties");
			}
		}
		return MIME_TYPES;
	}

	/**
	 * Get MIME type from file name extension, if possible
	 *
	 * @param uri the string representing a file
	 * @return the connected mime/type
	 */
	public static String getMimeTypeForFile(String uri) {
		int dot = uri.lastIndexOf('.');
		String mime = null;
		if (dot >= 0) {
			mime = mimeTypes().get(uri.substring(dot + 1).toLowerCase());
		}
		return mime == null ? "application/octet-stream" : mime;
	}

	public final static void safeClose(Object closeable) {
		try {
			if (closeable != null) {
				if (closeable instanceof Closeable) {
					((Closeable) closeable).close();
				}
				else if (closeable instanceof Socket) {
					((Socket)closeable).close();
				} else if (closeable instanceof ServerSocket) {
					((ServerSocket)closeable).close();
				} else {
					throw new IllegalArgumentException("Unknown object to close");
				}
			}
		}
		catch (Exception e) {
			NanoHTTPD.LOGGER.log(Level.SEVERE, "Could not close", e);
		}
	}

	/**
	 * Start the server
	 *
	 * @throws IOException if the socket is in use.
	 */
	public void start() throws IOException {
		start(NanoHTTPD.SOCKET_READ_TIMEOUT);
	}

	/**
	 * Starts the server in (in setDaemon(true) mode).
	 *
	 * @param timeout timeout to use for socket connections
	 */
	public void start(final int timeout) throws IOException {
		start(timeout, true);
	}

	/**
	 * Start the server.
	 *
	 * @param timeout timeout to use for socket connections
	 * @param daemon start the thread daemon or not.
	 * @throws IOException if the socket is in use.
	 */
	public void start(final int timeout, boolean daemon) throws IOException {
		this.myServerSocket = this.getServerSocketFactory().create();
		this.myServerSocket.setReuseAddress(true);

		ServerRunnable serverRunnable = createServerRunnable(timeout);
		this.myThread = new Thread(serverRunnable);
		this.myThread.setDaemon(true);
		this.myThread.setName("NanoHttpd Main Listener");
		this.myThread.start();

		while (!serverRunnable.hasBinded() && serverRunnable.getBindException() == null) {
			try {
				Thread.sleep(10L);
			}
			catch (Throwable e) {
				// android this may not be allowed, that's why we
				// catch throwable the wait should be very short because we are
				// just waiting for the bind of the socket
			}
		}

		if (serverRunnable.getBindException() != null) {
			throw serverRunnable.getBindException();
		}
	}

	/**
	 * Stop the server.
	 */
	public void stop() {
		try {
			safeClose(this.myServerSocket);
			this.asyncRunner.closeAll();
			if (this.myThread != null) {
				this.myThread.join();
			}
		}
		catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Could not stop all connections", e);
		}
	}

	/**
	 * Instantiate the server runnable, can be overridden by subclasses to
	 * provide a subclass of the ServerRunnable
	 *
	 * @param timeout the socket timeout to use.
	 * @return the server runnable.
	 */
	protected ServerRunnable createServerRunnable(final int timeout) {
		return new ServerRunnable(this, timeout);
	}

	/**
	 * Override this to customize the server.
	 *
	 * <p>(By default, this return a 404 "Not Found" plain text error response.)
	 *
	 * @param session The HTTP session
	 * @return HTTP Response, see class Response for details
	 */
	@Deprecated
	protected Response serve(IHTTPSession session) {
		return Response.newFixedLengthResponse(Status.NOT_FOUND, NanoHTTPD.MIME_PLAINTEXT, "Not Found");
	}

	public final boolean wasStarted() {
		return this.myServerSocket != null && this.myThread != null;
	}

	private static void loadMimeTypes(Map<String, String> result, String resourceName) {
		try {
			Enumeration<URL> resources = NanoHTTPD.class.getClassLoader().getResources(resourceName);
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				Properties properties = new Properties();
				InputStream stream = null;
				try {
					stream = url.openStream();
					properties.load(stream);
				}
				catch (IOException e) {
					LOGGER.log(Level.SEVERE, "could not load mimetypes from " + url, e);
				} finally {

				}
			}
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Getter
	public static final class ResponseException extends Exception {
		private static final long serialVersionUID = -1688629409805817325L;

		private final Status status;

		public ResponseException(Status status, String message) {
			super(message);
			this.status = status;
		}

		public ResponseException(Status status, String message, Throwable cause) {
			super(message, cause);
			this.status = status;
		}
	}
}
