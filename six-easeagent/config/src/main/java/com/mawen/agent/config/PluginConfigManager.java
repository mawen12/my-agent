package com.mawen.agent.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.IConfigFactory;
import com.mawen.agent.plugin.api.config.IPluginConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class PluginConfigManager implements IConfigFactory {

	private final Config config;
	private final Map<Key, PluginSourceConfig> pluginSourceConfigs;
	private final Map<Key, PluginConfig> pluginConfigs;

	public PluginConfigManager(Config config, Map<Key, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
		this.config = Objects.requireNonNull(config, "configs must not be null");
		this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null");
		this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null");
	}

	public static PluginConfigManager.Builder builder(Configs configs) {
		PluginConfigManager pluginConfigManager = new PluginConfigManager(configs, new HashMap<>(), new HashMap<>());
		return pluginConfigManager.new Builder();
	}

	@Override
	public String getConfig(String property) {
		return config.getString(property);
	}

	@Override
	public String getConfig(String property, String defaultValue) {
		return config.getString(property, defaultValue);
	}

	@Override
	public IPluginConfig getConfig(String domain, String namespace, String name) {
		return getConfig(domain, namespace, name, null);
	}

	public synchronized PluginConfig getConfig(String domain, String namespace, String id, PluginConfig oldConfig) {
		Key key = new Key(domain, namespace, id);
		PluginConfig pluginConfig = pluginConfigs.get(key);
		if (pluginConfig != null) {
			return pluginConfig;
		}
		Map<String, String> globalConfig = getGlobalConfig(domain, id);
		Map<String, String> coverConfig = getCoverConfig(domain, namespace, id);
		PluginConfig newPluginConfig = PluginConfig.build(domain, id, globalConfig, namespace, coverConfig, oldConfig);
		pluginConfigs.put(key, newPluginConfig);
		return newPluginConfig;
	}

	private Map<String, String> getGlobalConfig(String domain, String id) {
		return getConfigSource(domain, ConfigConst.PLUGIN_GLOBAL, id);
	}

	private Map<String, String> getCoverConfig(String domain, String namespace, String id) {
		return getConfigSource(domain, namespace, id);
	}

	private Map<String, String> getConfigSource(String domain, String namespace, String id) {
		PluginSourceConfig sourceConfig = pluginSourceConfigs.get(new Key(domain, namespace, id));
		if (sourceConfig == null) {
			return Collections.emptyMap();
		}
		return sourceConfig.getProperties();
	}

	private Set<Key> keys(Set<String> keys) {
		return keys.stream()
				.filter(ConfigUtils::isPluginConfig)
				.map(ConfigUtils::pluginProperty)
				.map(property -> new Key(property.domain(), property.namespace(), property.id()))
				.collect(Collectors.toSet());
	}

	@Override
	public Config getConfig() {
		return config;
	}

	public class Builder {
		public PluginConfigManager build() {
			synchronized (PluginConfigManager.this) {
				Map<String, String> sources = config.getConfigs();
				Set<Key> sourceKeys = keys(sources.keySet());
				for (Key sourceKey : sourceKeys) {
					pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain(), sourceKey.namespace(), sourceKey.id(), sources));
				}
				for (Key key : pluginSourceConfigs.keySet()) {
					getConfig(key.domain(), key.namespace(), key.id());
				}
			}
			return PluginConfigManager.this;
		}
	}

	public static class Key {
		private final String domain;
		private final String namespace;
		private final String id;

		public Key(String domain, String namespace, String id) {
			this.domain = domain;
			this.namespace = namespace;
			this.id = id;
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
	}

}
