package com.mawen.agent.plugin.report.tracing;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * from zipkin2.Endpoint
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class Endpoint {
	String serviceName;
	String ipV4;
	String ipV6;
	int port;

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public void setIpV4(String ipV4) {
		this.ipV4 = ipV4;
	}

	public void setIpV6(String ipV6) {
		this.ipV6 = ipV6;
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * Lower-case label of this node in the service graph, such as "favstar". Leave absent it unknown.
	 * This is a primary label for trace lookup and aggregation, so
	 * it should be intuitive and consistent. Many use a name from service discovery.
	 */
	public String serviceName() {
		return serviceName;
	}

	/**
	 * The text representation of the primary IPv4 address associated with this a connection.
	 * Ex. 192.168.99.100 Absent if unknown.
	 */
	public String ipV4() {
		return ipV4;
	}

	/**
	 * The text representation of the primary IPv6 address associated with this a connection.
	 * Ex. 2001:db8::c001 Absent if unknown.
	 *
	 * @see #ipV4() for mapped addresses
	 */
	public String ipV6() {
		return ipV6;
	}

	/**
	 * Port of the IP's socket or null, if not known
	 *
	 * @see InetSocketAddress#getPort()
	 */
	public int getPort() {
		return port;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Endpoint endpoint)) return false;

		return port == endpoint.port && Objects.equals(serviceName, endpoint.serviceName) && Objects.equals(ipV4, endpoint.ipV4) && Objects.equals(ipV6, endpoint.ipV6);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(serviceName);
		result = 31 * result + Objects.hashCode(ipV4);
		result = 31 * result + Objects.hashCode(ipV6);
		result = 31 * result + port;
		return result;
	}
}
