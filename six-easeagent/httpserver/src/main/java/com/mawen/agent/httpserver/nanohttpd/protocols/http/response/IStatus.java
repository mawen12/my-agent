package com.mawen.agent.httpserver.nanohttpd.protocols.http.response;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IStatus {

	String getDescription();

	int getRequestStatus();
}
