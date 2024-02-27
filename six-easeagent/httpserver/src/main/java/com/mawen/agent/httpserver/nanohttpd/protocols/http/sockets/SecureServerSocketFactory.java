package com.mawen.agent.httpserver.nanohttpd.protocols.http.sockets;

import java.io.IOException;
import java.net.ServerSocket;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import com.mawen.agent.httpserver.nanohttpd.util.IFactoryThrowing;

/**
 * Create a new SSLServerSocket
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class SecureServerSocketFactory implements IFactoryThrowing<ServerSocket, IOException> {

	private SSLServerSocketFactory sslServerSocketFactory;
	private String[] sslProtocols;

	public SecureServerSocketFactory(SSLServerSocketFactory sslServerSocketFactory, String[] sslProtocols) {
		this.sslServerSocketFactory = sslServerSocketFactory;
		this.sslProtocols = sslProtocols;
	}

	@Override
	public ServerSocket create() throws IOException {
		SSLServerSocket ss = (SSLServerSocket) this.sslServerSocketFactory.createServerSocket();
		if (this.sslProtocols != null) {
			ss.setEnabledProtocols(this.sslProtocols);
		} else {
			ss.setEnabledProtocols(ss.getSupportedProtocols());
		}
		ss.setUseClientMode(false);
		ss.setWantClientAuth(false);
		ss.setNeedClientAuth(false);
		return ss;
	}
}
