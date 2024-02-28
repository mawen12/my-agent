package com.mawen.agent.httpserver.nanohttpd.protocols.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Level;

import lombok.Getter;

/**
 * The runnable that will be used for the main listening thread.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
public class ServerRunnable implements Runnable {

	private NanoHTTPD httpd;
	private final int timeout;
	@Getter
	private IOException bindException;
	@Getter
	private boolean hasBinded = false;

	public ServerRunnable(NanoHTTPD httpd, int timeout) {
		this.httpd = httpd;
		this.timeout = timeout;
	}

	@Override
	public void run() {
		try {
			httpd.getMyServerSocket().bind(httpd.hostname != null ? new InetSocketAddress(httpd.hostname, httpd.myPort) : new InetSocketAddress(httpd.myPort));
			hasBinded = true;
		}
		catch (IOException e) {
			this.bindException = e;
			return;
		}
		do {
			try {
				final Socket finalAccept = httpd.getMyServerSocket().accept();
				if (this.timeout > 0) {
					finalAccept.setSoTimeout(this.timeout);
				}
				final InputStream inputStream = finalAccept.getInputStream();
				httpd.asyncRunner.exec(httpd.createClientHandler(finalAccept, inputStream));
			}
			catch (IOException e) {
				NanoHTTPD.LOGGER.log(Level.FINE, "Communication with the client broken", e);
			}
		} while (!httpd.getMyServerSocket().isClosed());
	}

	public boolean hasBinded() {
		return hasBinded;
	}
}
