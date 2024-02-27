package com.mawen.agent.httpserver;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IHttpHandler {

	String getPath();

	HttpResponse process(HttpRequest request, Map<String, String> uriParams);

	default int priority() {
		return 100;
	}
}
