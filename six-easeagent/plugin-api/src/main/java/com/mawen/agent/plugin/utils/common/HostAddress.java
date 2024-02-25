package com.mawen.agent.plugin.utils.common;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public abstract class HostAddress {
	private static final String LOCALHOST_NAME;
	private static final String IPV4;
	private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";

	static {
		IPV4 = initHostIpv4();
		LOCALHOST_NAME = initLocalHostname();
	}

	public static String localhost() {
		return LOCALHOST_NAME;
	}

	public static String getHostIpv4() {
		return IPV4;
	}

	public static String initHostIpv4() {
		try {
			return getHostIpv4(NetworkInterface.getNetworkInterfaces());
		}
		catch (Exception ignored) {
		}
		return "UnknownIP";
	}

	private static String getHostIpv4(Enumeration<NetworkInterface> interfaces) throws Exception {
		String ip;
		String secondaryIP = "";
		while (interfaces.hasMoreElements()) {
			NetworkInterface i = interfaces.nextElement();
			if (i.isLoopback()) {
				continue;
			}
			if (isPrimaryInterface(i)) {
				// We treat interface name which started with "en" or "eth" as primary interface.
				// We prefer to use address of primary interface as value of the `host_ipv4`
				ip = ipAddressFromInetAddress(i);
				if (!StringUtils.isEmpty(ip)) {
					return ip;
				}
			}
			else if (StringUtils.isEmpty(secondaryIP)) {
				secondaryIP = ipAddressFromInetAddress(i);
			}
		}

		return !StringUtils.isEmpty(secondaryIP) ? secondaryIP : "UnknownIP";
	}

	private static boolean isPrimaryInterface(NetworkInterface i) {
		return i.getName().startsWith("en") || i.getName().startsWith("eth");
	}

	private static String ipAddressFromInetAddress(NetworkInterface i) {
		Enumeration<InetAddress> ee = i.getInetAddresses();
		while(ee.hasMoreElements()) {
			InetAddress a = ee.nextElement();
			if (a instanceof Inet4Address) {
				if (!a.isMulticastAddress() && !a.isLoopbackAddress()) {
					return a.getHostAddress();
				}
			}
		}
		return "";
	}

	public static String initLocalHostname() {
		// copy from log4j NetUtils
		try {
			InetAddress addr = InetAddress.getLocalHost();
			return addr == null ? UNKNOWN_LOCALHOST : addr.getHostName();
		}
		catch (UnknownHostException e) {
			try {
				final Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				if (interfaces != null) { // NetworkInterface.getNetworkInterfaces impl is different in different jdk
					while (interfaces.hasMoreElements()) {
						final NetworkInterface nic = interfaces.nextElement();
						final Enumeration<InetAddress> addresses = nic.getInetAddresses();
						while (addresses.hasMoreElements()) {
							InetAddress address = addresses.nextElement();
							if (!address.isLoopbackAddress()) {
								final String hostname = address.getHostName();
								if (hostname != null) {
									return hostname;
								}
							}
						}
					}
				}
			}
			catch (SocketException ex) {
				return UNKNOWN_LOCALHOST;
			}
			return UNKNOWN_LOCALHOST;
		}
	}

	private HostAddress(){}

}
