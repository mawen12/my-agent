package com.mawen.agent.httpserver;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IHttpServer {

	void start(int port);

	void addHttpRoutes(List<IHttpHandler> agentHttpHandlers);

	void stop();
}
