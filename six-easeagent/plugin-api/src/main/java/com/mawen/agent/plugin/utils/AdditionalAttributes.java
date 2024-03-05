package com.mawen.agent.plugin.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.utils.common.HostAddress;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
@Getter
public class AdditionalAttributes {
	private static final Logger LOGGER = Agent.getLogger(AdditionalAttributes.class);

	public final Map<String, Object> attributes;

	public AdditionalAttributes(String serviceName, String systemName) {
		attributes = new HashMap<>();
		attributes.put("host_ipv4", getHostIpv4());
		attributes.put("service", serviceName);
		attributes.put("system", systemName);
		attributes.put("host_name", getHostName());
	}

	public AdditionalAttributes(String serviceName) {
		this(serviceName, "none");
	}

	public static String getLocalIP() {
		return HostAddress.getHostIpv4();
	}

	public static String getHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		}
		catch (UnknownHostException e) {
			String host = e.getMessage();
			if (host != null) {
				int colon = host.indexOf(':');
				if (colon > 0) {
					host = host.substring(0, colon);
				}
			}
			return "UnknownHost";
		}
	}

	private static boolean isEmpty(String text) {
		return text == null || text.trim().length() == 0;
	}

	private static boolean isPrimaryInterface(NetworkInterface i) {
		return i.getName().startsWith("en") || i.getName().startsWith("eth");
	}

	private String getHostIpv4() {
		return HostAddress.getHostIpv4();
	}
}
