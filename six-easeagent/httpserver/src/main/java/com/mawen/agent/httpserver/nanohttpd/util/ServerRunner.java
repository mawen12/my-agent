package com.mawen.agent.httpserver.nanohttpd.util;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mawen.agent.httpserver.nanohttpd.protocols.http.NanoHTTPD;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.ServerRunnable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class ServerRunner {
	private static final Logger LOG = Logger.getLogger(ServerRunner.class.getName());

	public static void executeInstance(NanoHTTPD server) {
		try {
			server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
		}
		catch (IOException e) {
			System.err.println("Couldn't start server:\n" + e);
			System.exit(-1);
		}

		System.out.println("Server started, Hit Enter to stop.\n");

		try {
			System.in.read();
		}
		catch (Throwable e) {
		}

		server.stop();
		System.out.println("Server stopped.\n");
	}

	public static <T extends NanoHTTPD> void run(Class<T> serverClass) {
		try {
			executeInstance(serverClass.newInstance());
		}
		catch (Exception e) {
			ServerRunner.LOG.log(Level.SEVERE, "Could not create server", e);
		}
	}
}
