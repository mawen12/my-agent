package com.mawen.agent.httpserver.jdk;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executors;

import com.mawen.agent.httpserver.IHttpHandler;
import com.mawen.agent.httpserver.IHttpServer;
import com.mawen.agent.plugin.async.AgentThreadFactory;
import com.sun.net.httpserver.HttpServer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class AgentHttpServerV2 implements IHttpServer {

	RootContextHandler httpRootHandler;
	HttpServer server;

	@Override
	public void start(int port) {
		try {
			this.server = HttpServer.create(new InetSocketAddress("localhost", port), 0);
			this.httpRootHandler = new RootContextHandler();
			this.server.createContext("/", this.httpRootHandler);

			var executorService = Executors.newFixedThreadPool(1, new AgentThreadFactory());
			this.server.setExecutor(executorService);

			this.server.start();
		}
		catch (IOException e) {

		}
	}

	@Override
	public void addHttpRoutes(List<IHttpHandler> agentHttpHandlers) {
		agentHttpHandlers.forEach(handler -> this.httpRootHandler.addRoute(handler));
	}

	@Override
	public void stop() {
		this.server.stop(0);
	}
}
