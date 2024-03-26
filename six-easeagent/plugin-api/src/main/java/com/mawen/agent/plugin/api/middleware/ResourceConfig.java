package com.mawen.agent.plugin.api.middleware;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mawen.agent.plugin.utils.SystemEnv;
import com.mawen.agent.plugin.utils.common.JsonUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ResourceConfig {

	private String username;
	private String password;
	private String uris;
	private final List<String> uriList = new ArrayList<>();
	private final List<HostAndPort> hostAndPorts = new ArrayList<>();

	public static ResourceConfig getResourceConfig(String env, boolean needParse) {
		String str = SystemEnv.get(env);
		if (str == null) {
			return null;
		}
		ResourceConfig resourceConfig = JsonUtil.toObject(str, new TypeReference<ResourceConfig>() {});
		resourceConfig.parseHostAndPort(needParse);
		if (resourceConfig.hasUrl()) {
			return resourceConfig;
		}
		return null;
	}

	private void parseHostAndPort(boolean needParse) {
		if (!needParse) {
			uriList.add(this.uris);
			return;
		}
		if (uris == null || uris.isEmpty()) {
			return;
		}
		String[] list = uris.split(",");
		for (String uri : list) {
			uriList.add(uri);
			int begin = uri.indexOf(":");
			int end = uri.lastIndexOf(":");
			if (begin == end) {
				String[] arr = uri.split(":");
				HostAndPort obj = new HostAndPort();
				obj.setHost(arr[0]);
				obj.setPort(Integer.parseInt(arr[1]));
				this.hostAndPorts.add(obj);
			}
		}
	}

	public HostAndPort getFirstHostAndPort() {
		if (hostAndPorts.isEmpty()) {
			return null;
		}
		return this.hostAndPorts.get(0);
	}

	public String getFirstUri() {
		if (uriList == null || uriList.isEmpty()) {
			return null;
		}

		return uriList.get(0);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean hasUrl() {
		return this.uris != null && !this.uris.isEmpty();
	}

	public String getUris() {
		return uris;
	}

	public void setUris(String uris) {
		this.uris = uris;
	}

	public List<String> getUriList() {
		return uriList;
	}

	public List<HostAndPort> getHostAndPorts() {
		return hostAndPorts;
	}

	public static class HostAndPort {
		private String host;
		private Integer port;

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public Integer getPort() {
			return port;
		}

		public void setPort(Integer port) {
			this.port = port;
		}

		@Override
		public final boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof HostAndPort)) return false;

			HostAndPort that = (HostAndPort) o;
			return Objects.equals(host, that.host) && Objects.equals(port, that.port);
		}

		@Override
		public int hashCode() {
			int result = Objects.hashCode(host);
			result = 31 * result + Objects.hashCode(port);
			return result;
		}

		public String uri() {
			return host + ":" + port;
		}
	}
}
