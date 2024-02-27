package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.sockets.DefaultServerSocketFactory;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.threading.IAsyncRunner;
import com.mawen.agent.httpserver.nanohttpd.util.IFactoryThrowing;
import com.mawen.agent.httpserver.nanohttpd.util.IHandler;
import lombok.Getter;
import org.apache.kafka.common.metrics.Stat;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class NanoHTTPD {
	public static final Logger LOGGER = Logger.getLogger(NanoHTTPD.class.getName());
	public static final String CONTENT_DISPOSITION_REGEX = "([ |\t]*Content-Disposition[ |\t]*:)(.*)";
	public static final Pattern CONTENT_DISPOSITION_PATTERN = Pattern.compile(CONTENT_DISPOSITION_REGEX, Pattern.CASE_INSENSITIVE);
	public static final String CONTENT_TYPE_REGEX = "([ |\t]*content-type[ |\t]*:)(.*)";
	public static final Pattern CONTENT_TYPE_PATTERN = Pattern.compile(CONTENT_TYPE_REGEX, Pattern.CASE_INSENSITIVE);
	public static final String CONTENT_DISPOSITION_ATTRIBUTE_REGEX = "[ |\t]*([a-zA-z]*)[ |\t]*=[ |\t]*['|\"]([^\"^']*)['|\"]";
	public static final Pattern CONTENT_DISPOSITION_ATTRIBUTE_PATTERN = Pattern.compile(CONTENT_DISPOSITION_ATTRIBUTE_REGEX, Pattern.CASE_INSENSITIVE);

	public static final int SOCKET_READ_TIMEOUT = 5000;
	public static final String MIME_PLAINTEXT = "text/plain";
	public static final String MIME_HTML = "text/html";
	protected static Map<String, String> MIME_TYPES;
	private static final String QUERY_STRING_PARAMETER = "NanoHttpd.QUERY_STRING";

	public final String hostname;
	public final int myPort;
	private volatile ServerSocket myServerSocket;
	private IFactoryThrowing<ServerSocket, IOException> serverSocketFactory = new DefaultServerSocketFactory();
	private Thread thread;
	private IHandler<IHttpSession, Response> httpHandler;
	private List<IHandler<IHttpSession, Response>> interceptors = new ArrayList<>(4);

	protected IAsyncRunner asyncRunner;
	private IFactory<>

	public ServerSocket getMyServerSocket() {
		return myServerSocket;
	}

	public static Map<String, String> mimeTypes() {
		if (MIME_TYPES == null) {
			MIME_TYPES = new HashMap<>();

		}
	}

	public static final void safeClose(Object closeable) {
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
