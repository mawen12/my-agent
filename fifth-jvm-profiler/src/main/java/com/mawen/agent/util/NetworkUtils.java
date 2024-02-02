package com.mawen.agent.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class NetworkUtils {

	private static final String ENV_COMPUTERNAME = "COMPUTERNAME";
	private static final String ENV_HOSTNAME = "HOSTNAME";

	public static String getLocalHostName() {

		try {
			Map<String, String> env = System.getenv();
			if (env.containsKey(ENV_COMPUTERNAME)) {
				return env.get(ENV_COMPUTERNAME);
			}
			else if (env.containsKey(ENV_HOSTNAME)) {
				return env.get(ENV_HOSTNAME);
			}
			else {
				return InetAddress.getLocalHost().getHostName();
			}
		}
		catch (UnknownHostException e) {
			return "unknown_localhost_name";
		}
	}
}
