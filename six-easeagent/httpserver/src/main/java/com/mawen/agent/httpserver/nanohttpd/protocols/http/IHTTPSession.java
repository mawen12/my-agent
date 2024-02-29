package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.content.CookieHandler;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.request.Method;

/**
 * Handles one session, i.e. parses the HTTP request and returns the response.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IHTTPSession {

	void execute() throws IOException;

	CookieHandler getCookies();

	Map<String, String> getHeaders();

	InputStream getInputStream();

	Method getMethod();

	/**
	 * This method will only return the first value for a given parameters.
	 * You will want to use {@link #getParameters()} if you expect multiple values for a given key.
	 *
	 * @deprecated use {@link #getParameters()} instead.
	 */
	@Deprecated
	Map<String, String> getParams();

	Map<String, List<String>> getParameters();

	String getQueryParameterString();

	/**
	 * @return thr path part of the URL.
	 */
	String getUri();

	/**
	 * Adds the files in the request body to the files map.
	 *
	 * @param files map to modify
	 */
	void parseBody(Map<String, String> files) throws IOException, NanoHTTPD.ResponseException;

	/**
	 * Get the remote ip address of the requester.
	 *
	 * @return the IP address.
	 */
	String getRemoteIpAddress();

}
