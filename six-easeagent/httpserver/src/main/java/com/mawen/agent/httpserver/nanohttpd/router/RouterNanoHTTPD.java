package com.mawen.agent.httpserver.nanohttpd.router;

import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import com.mawen.agent.httpserver.jdk.UriResource;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import org.apache.kafka.common.metrics.Stat;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class RouterNanoHTTPD extends NanoHTTPD {

	private static final Logger LOGGER = Logger.getLogger(RouterNanoHTTPD.class.getName());



	public interface UriResponder {
		Response get(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response put(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response post(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response delete(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);

		Response other(UriResource uriResource, Map<String, String> uriParams, IHTTPSession session);
	}

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
		public Response other(UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return get(uriResource, urlParams, session);
		}
	}

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
	}

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
