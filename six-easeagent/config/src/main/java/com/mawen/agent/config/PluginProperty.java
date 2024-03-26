package com.mawen.agent.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class PluginProperty {
	private final String domain;
	private final String namespace;
	private final String id;
	private final String property;

	public PluginProperty(String domain, String namespace, String id, String property) {
		this.domain = domain;
		this.namespace = namespace;
		this.id = id;
		this.property = property;
	}

	public String domain() {
		return domain;
	}

	public String namespace() {
		return namespace;
	}

	public String id() {
		return id;
	}

	public String property() {
		return property;
	}
}

