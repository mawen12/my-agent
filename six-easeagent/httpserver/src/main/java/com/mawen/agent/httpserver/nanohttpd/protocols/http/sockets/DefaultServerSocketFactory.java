package com.mawen.agent.httpserver.nanohttpd.protocols.http.sockets;

import java.io.IOException;
import java.net.ServerSocket;

import com.mawen.agent.httpserver.nanohttpd.util.IFactoryThrowing;

/**
 * Creates a normal ServerSocket for TCP connections
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultServerSocketFactory implements IFactoryThrowing<ServerSocket, IOException> {

	@Override
	public ServerSocket create() throws IOException {
		return new ServerSocket();
	}
}
