package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class ClientHandler implements Runnable {
	private final NanoHTTPD httpd;
	private final InputStream inputStream;
	private final Socket acceptSocket;

	public ClientHandler(NanoHTTPD httpd, InputStream inputStream, Socket acceptSocket) {
		this.httpd = httpd;
		this.inputStream = inputStream;
		this.acceptSocket = acceptSocket;
	}

	public NanoHTTPD httpd() {
		return httpd;
	}

	public InputStream inputStream() {
		return inputStream;
	}

	public Socket acceptSocket() {
		return acceptSocket;
	}

	@Override
	public void run() {
		OutputStream outputStream = null;
		try {
			outputStream = this.acceptSocket.getOutputStream();

		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		NanoHTTPD.safeClose(this.inputStream);
		NanoHTTPD.safeClose(this.acceptSocket);
	}
}
