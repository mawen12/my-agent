package com.mawen.agent.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.IConfigFactory;
import com.mawen.agent.plugin.api.config.IPluginConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class PluginConfigManager implements IConfigFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigManager.class);

	private Runnable shutdownRunnable;
	private final Configs configs;
	private final Map<Key, PluginSourceConfig> pluginSourceConfigs;
	private final Map<Key, PluginConfig> pluginConfigs;

	public PluginConfigManager(Configs configs, Map<Key, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
		this.configs = Objects.requireNonNull(configs, "configs must not be null");
		this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null");
		this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null");
	}

	public static PluginConfigManager.Builder builder(Configs configs) {
		PluginConfigManager pluginConfigManager = new PluginConfigManager(configs, new HashMap<>(), new HashMap<>());
		return pluginConfigManager.new Builder();
	}

	@Override
	public Config getConfig() {
		return this.configs;
	}

	@Override
	public String getConfig(String property) {
		return configs.getString(property);
	}

	@Override
	public String getConfig(String property, String defaultValue) {
		return configs.getString(property, defaultValue);
	}

	@Override
	public IPluginConfig getConfig(String domain, String namespace, String name) {
		return getConfig(domain,namespace,name, null);
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

	public void shutdown() {
		shutdownRunnable.run();
	}

	protected synchronized void onChange(Map<String, String> sources) {
		Set<Key> sourceKeys = keys(sources.keySet());
		Map<String, String> newSources = buildNewSources(sourceKeys, sources);
		for (Key sourceKey : sourceKeys) {
			pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.getDomain(), sourceKey.getNamespace(), sourceKey.getId(), newSources));
		}
		Set<Key> changeKeys = buildChangeKeys(sourceKeys);

		for (Key changeKey : changeKeys) {
			final PluginConfig oldConfig = pluginConfigs.remove(changeKey);
			final PluginConfig newConfig = getConfig(changeKey.getDomain(), changeKey.getNamespace(), changeKey.id, oldConfig);
			if (oldConfig == null) {
				continue;
			}
			try {
				oldConfig.foreachConfigChangeListener(listener -> listener.onChange(oldConfig, newConfig));
			}
			catch (Exception e) {
				LOGGER.warn("change config<{}> fail: {}",changeKey.toString(), e.getMessage());
			}
		}
	}

	private Set<Key> keys(Set<String> keys) {
		Set<Key> propertyKeys = new HashSet<>();
		for (String k : keys) {
			if (!ConfigUtils.isPluginConfig(k)) {
				continue;
			}
			PluginProperty property = ConfigUtils.pluginProperty(k);
			Key key = new Key(property.getDomain(), property.getNamespace(), property.getId());
			propertyKeys.add(key);
		}
		return propertyKeys;
	}

	private Map<String, String> buildNewSources(Set<Key> sourceKeys, Map<String, String> sources) {
		Map<String, String> newSources = new HashMap<>();
		for (Key sourceKey : sourceKeys) {
			PluginSourceConfig pluginSourceConfig = pluginSourceConfigs.get(sourceKey);
			if (pluginSourceConfig == null) {
				continue;
			}
			newSources.putAll(pluginSourceConfig.getSource());
		}
		newSources.putAll(sources);
		return newSources;
	}

	private Set<Key> buildChangeKeys(Set<Key> sourceKeys) {
		Set<Key> changeKeys = new HashSet<>(sourceKeys);
		for (Key key : sourceKeys) {
			if (!ConfigUtils.isGlobal(key.getNamespace())) {
				continue;
			}
			for (Key oldKey : pluginConfigs.keySet()) {
				if (!key.id.equals(oldKey.id)) {
					continue;
				}
				changeKeys.add(oldKey);
			}
		}
		return changeKeys;
	}

	class Key {
		private final String domain;
		private final String namespace;
		private final String id;

		public Key(String domain, String namespace, String id) {
			this.domain = domain;
			this.namespace = namespace;
			this.id = id;
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

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Key key)) return false;

			if (!Objects.equals(domain, key.domain)) return false;
			if (!Objects.equals(namespace, key.namespace)) return false;
			return Objects.equals(id, key.id);
		}

		@Override
		public int hashCode() {
			int result = domain != null ? domain.hashCode() : 0;
			result = 31 * result + (namespace != null ? namespace.hashCode() : 0);
			result = 31 * result + (id != null ? id.hashCode() : 0);
			return result;
		}

		@Override
		public String toString() {
			return "Key{" +
					"domain='" + domain + '\'' +
					", namespace='" + namespace + '\'' +
					", id='" + id + '\'' +
					'}';
		}
	}

	public class Builder {
		public PluginConfigManager build() {
			synchronized (PluginConfigManager.this) {
				Map<String, String> sources = configs.getConfigs();
				Set<Key> sourceKeys = keys(sources.keySet());
				for (Key sourceKey : sourceKeys) {
					pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.getDomain(), sourceKey.getNamespace(), sourceKey.getId(), sources));
				}
				for (Key key : pluginSourceConfigs.keySet()) {
					getConfig(key.getDomain(), key.getNamespace(), key.getId());
				}
				shutdownRunnable = configs.addChangeListener(new ChangeListener());
			}
			return PluginConfigManager.this;
		}
	}

	class ChangeListener implements ConfigChangeListener {
		@Override
		public void onChange(List<ChangeItem> list) {
			Map<String, String> sources = new HashMap<>();
			for (ChangeItem item : list) {
				sources.put(item.getFullName(), item.getNewValue());
			}
			PluginConfigManager.this.onChange(sources);
		}
	}
}
