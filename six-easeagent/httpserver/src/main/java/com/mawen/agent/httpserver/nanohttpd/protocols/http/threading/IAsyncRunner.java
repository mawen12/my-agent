package com.mawen.agent.httpserver.nanohttpd.protocols.http.threading;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.ClientHandler;

/**
 * Pluggable strategy for asynchronously executing requests.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IAsyncRunner {

	void closeAll();

	void closed(ClientHandler clientHandler);

	void exec(ClientHandler handler);
}
