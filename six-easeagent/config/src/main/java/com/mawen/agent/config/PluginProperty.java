package com.mawen.agent.config;

import java.util.Objects;

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

	public String getDomain() {
		return domain;
	}

	public String getNamespace() {
		return namespace;
	}

	public String getId() {
		return id;
	}

	public String getProperty() {
		return property;
	}

	@Override
	public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PluginProperty that)) return false;

		return Objects.equals(domain, that.domain) && Objects.equals(namespace, that.namespace) && Objects.equals(id, that.id) && Objects.equals(property, that.property);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(domain);
		result = 31 * result + Objects.hashCode(namespace);
		result = 31 * result + Objects.hashCode(id);
		result = 31 * result + Objects.hashCode(property);
		return result;
	}
}
