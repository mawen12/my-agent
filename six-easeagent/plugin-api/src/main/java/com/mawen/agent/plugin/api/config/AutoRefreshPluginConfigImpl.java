package com.mawen.agent.plugin.api.config;

import java.util.List;
import java.util.Set;

/**
 * a base AutoRefreshConfig
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AutoRefreshPluginConfigImpl implements IPluginConfig, AutoRefreshPluginConfig {
	protected volatile IPluginConfig config;

	@Override
	public String domain() {
		return config.domain();
	}

	@Override
	public String namespace() {
		return config.namespace();
	}

	@Override
	public String id() {
		return config.id();
	}

	@Override
	public boolean hasProperty(String property) {
		return config.hasProperty(property);
	}

	@Override
	public String getString(String property) {
		return config.getString(property);
	}

	@Override
	public Integer getInt(String property) {
		return config.getInt(property);
	}

	@Override
	public Boolean getBoolean(String property) {
		return config.getBoolean(property);
	}

	@Override
	public Double getDouble(String property) {
		return config.getDouble(property);
	}

	@Override
	public Long getLong(String property) {
		return config.getLong(property);
	}

	@Override
	public List<String> getStringList(String property) {
		return config.getStringList(property);
	}

	@Override
	public IPluginConfig getGlobal() {
		return config.getGlobal();
	}

	@Override
	public Set<String> keySet() {
		return config.keySet();
	}

	@Override
	public void addChangeListener(PluginConfigChangeListener listener) {
		config.addChangeListener(listener);
	}

	@Override
	public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
		this.config = newConfig;
	}

	public IPluginConfig getConfig() {
		return config;
	}
}
