package com.mawen.agent.httpserver.nanohttpd.protocols.http.threading;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.ClientHandler;

/**
 * Default threading strategy for NanoHTTPD
 *
 * <p>By default, the server spawns a new Thread for every incoming request.
 * These are set to <i>daemon</i> status, and named according to the request number.
 * The name is useful when profiling the application.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultAsyncRunner implements IAsyncRunner {

	protected long requestCount;
	private final List<ClientHandler> running = Collections.synchronizedList(new ArrayList<>());

	public long getRequestCount() {
		return requestCount;
	}

	@Override
	public void closeAll() {
		for (ClientHandler clientHandler : new ArrayList<>(this.running)) {
			clientHandler.close();
		}
	}

	@Override
	public void closed(ClientHandler clientHandler) {
		this.running.remove(clientHandler);
	}

	@Override
	public void exec(ClientHandler handler) {
		++this.requestCount;
		this.running.add(handler);
		createThread(handler).start();
	}

	protected Thread createThread(ClientHandler clientHandler) {
		Thread t = new Thread(clientHandler);
		t.setDaemon(true);
		t.setName("NanoHttpd Request Processor (#" + this.requestCount + ")");
		return t;
	}
}
