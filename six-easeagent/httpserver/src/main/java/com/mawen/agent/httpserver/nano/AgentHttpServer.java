package com.mawen.agent.httpserver.nano;

import java.io.IOException;

import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class AgentHttpServer extends RouterNanoHTTPD {

	public static String JSON_TYPE = "application/json";

	public AgentHttpServer(int port) {
		super(port);
		this.addMappings();
		Runtime.getRuntime().addShutdownHook(new Thread(this::stop));
	}

	public void addHttpRoute(AgentHttpHandler handler) {
		this.addRoute(handler.getPath(), handler.getClass());
	}

	public void startServer() {
		try {
			this.start(5000, true);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
