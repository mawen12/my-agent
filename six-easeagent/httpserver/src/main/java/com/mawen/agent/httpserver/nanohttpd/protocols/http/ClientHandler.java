package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public record ClientHandler(NanoHTTPD httpd, InputStream inputStream, Socket acceptSocket) implements Runnable {

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
