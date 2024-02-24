package com.mawen.agent.plugin.bridge;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class AgentInfo {
	private final String type;
	private final String version;

	public AgentInfo(String type, String version) {
		this.type = type;
		this.version = version;
	}

	public String getType() {
		return type;
	}

	public String getVersion() {
		return version;
	}
}
