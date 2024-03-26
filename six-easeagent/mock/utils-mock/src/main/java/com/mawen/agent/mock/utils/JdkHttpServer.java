package com.mawen.agent.mock.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class JdkHttpServer {
	public static final JdkHttpServer INSTANCE;

	static {
		try {
			INSTANCE = JdkHttpServer.builder().build();
		}
		catch (IOException e) {
			throw new RuntimeException("build JdkHttpServer fail.", e);
		}
	}

	private final int port;
	private final HttpServer server;
	private final String path;
	private final String url;
	private AtomicReference<Headers> lastHeaders = new AtomicReference<>();
	private AtomicReference<HttpExchange> lastHttpExchange = new AtomicReference<>();
	private Consumer<Headers> headersConsumer;
	private Consumer<HttpExchange> exchangeConsumer;

	public JdkHttpServer(int port, HttpServer server, String path) {
		this.port = port;
		this.server = server;
		this.path = path;
		this.url = String.format("http://127.0.0.1:%s%s", port, path);
	}

	public JdkHttpServer start() {
		HttpContext context = server.createContext(path);
		context.setHandler(JdkHttpServer.this::handleRequest);
		server.start();
		return this;
	}

	public void stop() {
		server.stop(0);
	}

	public void handleRequest(HttpExchange exchange) throws IOException {
		URI requestURI = exchange.getRequestURI();
		lastHeaders.set(exchange.getRequestHeaders());
		lastHttpExchange.set(exchange);
		if (this.headersConsumer != null) {
			this.headersConsumer.accept(exchange.getRequestHeaders());
		}
		if (this.exchangeConsumer != null) {
			this.exchangeConsumer.accept(exchange);
		}
		String response = String.format("This is the response at %s port: %s", requestURI, port);
		exchange.sendResponseHeaders(200, response.getBytes().length);
		OutputStream os = exchange.getResponseBody();
		os.write(response.getBytes());
		os.close();
	}

	public int getPort() {
		return port;
	}

	public HttpServer getServer() {
		return server;
	}

	public String getPath() {
		return path;
	}

	public String getUrl() {
		return url;
	}

	public AtomicReference<Headers> getLastHeaders() {
		return lastHeaders;
	}

	public AtomicReference<HttpExchange> getLastHttpExchange() {
		return lastHttpExchange;
	}

	public Consumer<Headers> getHeadersConsumer() {
		return headersConsumer;
	}

	public Consumer<HttpExchange> getExchangeConsumer() {
		return exchangeConsumer;
	}

	public static Builder builder() {
		return new Builder();
	}

	public void setHeadersConsumer(Consumer<Headers> headersConsumer) {
		this.headersConsumer = headersConsumer;
	}

	public void setExchangeConsumer(Consumer<HttpExchange> exchangeConsumer) {
		this.exchangeConsumer = exchangeConsumer;
	}

	public static class Builder {
		private int tryPortNum = 4;
		private int port = 0;
		private String path;
		private Consumer<Headers> headersConsumer;
		private Consumer<HttpExchange> exchangeConsumer;

		public void setTryPortNum(int tryPortNum) {
			this.tryPortNum = tryPortNum;
		}

		public Builder setPort(int port) {
			this.port = port;
			return this;
		}

		public Builder setPath(String path) {
			this.path = path;
			return this;
		}

		public Builder setHeadersConsumer(Consumer<Headers> headersConsumer) {
			this.headersConsumer = headersConsumer;
			return this;
		}

		public Builder setExchangeConsumer(Consumer<HttpExchange> exchangeConsumer) {
			this.exchangeConsumer = exchangeConsumer;
			return this;
		}

		public JdkHttpServer build() throws IOException {
			HttpServer httpServer = buildHttpServer();
			int p = httpServer.getAddress().getPort();
			String httpPath = path == null ? "/example" : path;
			JdkHttpServer jdkHttpServer = new JdkHttpServer(p, httpServer, httpPath);
			jdkHttpServer.setHeadersConsumer(headersConsumer);
			jdkHttpServer.setExchangeConsumer(exchangeConsumer);
			return jdkHttpServer;
		}

		private HttpServer buildHttpServer() throws IOException {
			if (port > 0) {
				return HttpServer.create(new InetSocketAddress(port), 0);
			}
			IOException ioException = null;
			for (int i = 0; i < tryPortNum; i++) {
				DatagramSocket socket = new DatagramSocket(0);
				int p = socket.getLocalPort();
				return HttpServer.create(new InetSocketAddress(p), 0);
			}
			throw ioException;
		}
	}
}
