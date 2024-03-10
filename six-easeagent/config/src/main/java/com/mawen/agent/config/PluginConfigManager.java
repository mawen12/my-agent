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
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class PluginConfigManager implements IConfigFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfigManager.class);

	private Runnable shutdownRunnable;
	@Getter
	private final Config config;
	private final Map<Key, PluginSourceConfig> pluginSourceConfigs;
	private final Map<Key, PluginConfig> pluginConfigs;

	public PluginConfigManager(Config config, Map<Key, PluginSourceConfig> pluginSourceConfigs, Map<Key, PluginConfig> pluginConfigs) {
		this.config = Objects.requireNonNull(config, "configs must not be null");
		this.pluginSourceConfigs = Objects.requireNonNull(pluginSourceConfigs, "pluginSourceConfigs must not be null");
		this.pluginConfigs = Objects.requireNonNull(pluginConfigs, "pluginConfigs must not be null");
	}

	public static PluginConfigManager.Builder builder(Configs configs) {
		var pluginConfigManager = new PluginConfigManager(configs, new HashMap<>(), new HashMap<>());
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
		return getConfig(domain,namespace,name, null);
	}

	public synchronized PluginConfig getConfig(String domain, String namespace, String id, PluginConfig oldConfig) {
		var key = new Key(domain, namespace, id);
		var pluginConfig = pluginConfigs.get(key);
		if (pluginConfig != null) {
			return pluginConfig;
		}
		var globalConfig = getGlobalConfig(domain, id);
		var coverConfig = getCoverConfig(domain, namespace, id);
		var newPluginConfig = PluginConfig.build(domain, id, globalConfig, namespace, coverConfig, oldConfig);
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
		var sourceConfig = pluginSourceConfigs.get(new Key(domain, namespace, id));
		if (sourceConfig == null) {
			return Collections.emptyMap();
		}
		return sourceConfig.getProperties();
	}

	public void shutdown() {
		shutdownRunnable.run();
	}

	protected synchronized void onChange(Map<String, String> sources) {
		var sourceKeys = keys(sources.keySet());
		var newSources = buildNewSources(sourceKeys, sources);
		for (var sourceKey : sourceKeys) {
			pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain(), sourceKey.namespace(), sourceKey.id(), newSources));
		}
		var changeKeys = buildChangeKeys(sourceKeys);

		for (var changeKey : changeKeys) {
			final var oldConfig = pluginConfigs.remove(changeKey);
			final var newConfig = getConfig(changeKey.domain(), changeKey.namespace(), changeKey.id(), oldConfig);
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
		var propertyKeys = new HashSet<Key>();
		for (var k : keys) {
			if (!ConfigUtils.isPluginConfig(k)) {
				continue;
			}
			var property = ConfigUtils.pluginProperty(k);
			var key = new Key(property.domain(), property.namespace(), property.id());
			propertyKeys.add(key);
		}
		return propertyKeys;
	}

	private Map<String, String> buildNewSources(Set<Key> sourceKeys, Map<String, String> sources) {
		var newSources = new HashMap<String, String>();
		for (var sourceKey : sourceKeys) {
			var pluginSourceConfig = pluginSourceConfigs.get(sourceKey);
			if (pluginSourceConfig == null) {
				continue;
			}
			newSources.putAll(pluginSourceConfig.getSource());
		}
		newSources.putAll(sources);
		return newSources;
	}

	private Set<Key> buildChangeKeys(Set<Key> sourceKeys) {
		var changeKeys = new HashSet<>(sourceKeys);
		for (var key : sourceKeys) {
			if (!ConfigUtils.isGlobal(key.namespace())) {
				continue;
			}
			for (var oldKey : pluginConfigs.keySet()) {
				if (!key.id.equals(oldKey.id)) {
					continue;
				}
				changeKeys.add(oldKey);
			}
		}
		return changeKeys;
	}

	record Key(String domain ,String namespace, String id) {}

	public class Builder {
		public PluginConfigManager build() {
			synchronized (PluginConfigManager.this) {
				var sources = config.getConfigs();
				var sourceKeys = keys(sources.keySet());
				for (var sourceKey : sourceKeys) {
					pluginSourceConfigs.put(sourceKey, PluginSourceConfig.build(sourceKey.domain(), sourceKey.namespace(), sourceKey.id(), sources));
				}
				for (var key : pluginSourceConfigs.keySet()) {
					getConfig(key.domain(), key.namespace(), key.id());
				}
				shutdownRunnable = config.addChangeListener(new ChangeListener());
			}
			return PluginConfigManager.this;
		}
	}

	class ChangeListener implements ConfigChangeListener {
		@Override
		public void onChange(List<ChangeItem> list) {
			var sources = new HashMap<String, String>();
			for (var item : list) {
				sources.put(item.fullName(), item.newValue());
			}
			PluginConfigManager.this.onChange(sources);
		}
	}
}
