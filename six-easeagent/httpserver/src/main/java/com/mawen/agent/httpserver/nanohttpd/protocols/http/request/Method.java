package com.mawen.agent.httpserver.nanohttpd.protocols.http.request;

/**
 * HTTP Request methods, with the ability to decode a {@code String} back to its enum value.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public enum Method {
	GET,
	PUT,
	POST,
	DELETE,
	HEAD,
	OPTIONS,
	TRACE,
	CONNECT,
	PATCH,
	PROPFIND,
	PROPPATCH,
	MKCOL,
	MOVE,
	COPY,
	LOCK,
	UNLOCK,
	NOTIFY,
	SUBSCRIBE,
	;

	public static Method lookup(String method) {
		if (method == null) {
			return null;
		}
		try {
			return valueOf(method);
		}
		catch (IllegalArgumentException e) {
			return null;
		}
	}
}
