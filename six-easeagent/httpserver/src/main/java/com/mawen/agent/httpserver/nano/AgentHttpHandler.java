package com.mawen.agent.httpserver.nano;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.request.Method;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public abstract class AgentHttpHandler extends RouterNanoHTTPD.DefaultHandler {

	protected String text;
	protected Set<Method> methods = new HashSet<>(Arrays.asList(Method.PUT, Method.POST));

	@Override
	public String getMimeType() {
		return null;
	}

	@Override
	public String getText() {
		return this.text;
	}

	@Override
	public IStatus getStatus() {
		return Status.OK;
	}

	@Override
	public Response get(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		return this.process(uriResource, urlParams, session);
	}

	public abstract Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session);

	protected String buildRequestBody(IHTTPSession session) {
		try {
			var files = new HashMap<String, String>();
			var method = session.getMethod();
			if (!methods.contains(method)) {
				return null;
			}
			session.parseBody(files);
			var content = files.get("content");
			if (content != null) {
				Path path = Paths.get(content);
				byte[] bytes = Files.readAllBytes(path);
				return new String(bytes, StandardCharsets.UTF_8);
			}
			return files.get("postData");
		}
		catch (IOException | NanoHTTPD.ResponseException e) {
			throw new RuntimeException(e);
		}
	}

	public abstract String getPath();
}
