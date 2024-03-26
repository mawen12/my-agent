package com.mawen.agent.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class PluginSourceConfig {
	private final String domain;
	private final String namespace;
	private final String id;
	private final Map<String, String> source;
	private final Map<PluginProperty, String> properties;

	public PluginSourceConfig(String domain, String namespace, String id, Map<String, String> source, Map<PluginProperty, String> properties) {
		this.domain = Objects.requireNonNull(domain, "domain must not be null");
		this.namespace = Objects.requireNonNull(namespace, "namespace must not be null");
		this.id = Objects.requireNonNull(id, "id must not be null");
		this.source = Objects.requireNonNull(source, "source must not be null");
		this.properties = Objects.requireNonNull(properties, "properties must not be null");
	}

	public static PluginSourceConfig build(String domain, String namespace, String id, Map<String, String> source) {
		Map<String, String> pluginSource = new HashMap<>();
		Map<PluginProperty, String> properties = new HashMap<PluginProperty, String>();
		for (Map.Entry<String, String> sourceEntry : source.entrySet()) {
			String key = sourceEntry.getKey();
			if (!ConfigUtils.isPluginConfig(key, domain, namespace, id)) {
				continue;
			}
			pluginSource.put(key, sourceEntry.getValue());
			PluginProperty property = ConfigUtils.pluginProperty(key);
			properties.put(property, sourceEntry.getValue());
		}

		return new PluginSourceConfig(domain, namespace, id, pluginSource, properties);
	}

	/**
	 * @since 0.0.2-SNAPSHOT
	 */
	public Map<String , String> getProperties() {
		Function<Map.Entry<PluginProperty, String>, String> keyGetter = entry -> entry.getKey().property();
		Function<Map.Entry<PluginProperty, String>, String> valueGetter = Map.Entry::getValue;

		return properties.entrySet()
				.stream()
				.collect(Collectors.toMap(keyGetter, valueGetter));
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

	public Map<String, String> getSource() {
		return source;
	}
}
