package com.mawen.agent.plugin.bridge;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.config.PluginConfigChangeListener;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class NoOpIPluginConfig implements IPluginConfig {
	public static NoOpIPluginConfig INSTANCE = new NoOpIPluginConfig("Noop", "Noop", "Noop");

	private final String domain;
	private final String namespace;
	private final String id;

	public NoOpIPluginConfig(String domain, String namespace, String id) {
		this.domain = domain;
		this.namespace = namespace;
		this.id = id;
	}

	@Override
	public String domain() {
		return domain;
	}

	@Override
	public String namespace() {
		return namespace;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public boolean hasProperty(String property) {
		return false;
	}

	@Override
	public String getString(String property) {
		return null;
	}

	@Override
	public Integer getInt(String property) {
		return null;
	}

	@Override
	public Boolean getBoolean(String property) {
		return null;
	}

	@Override
	public Double getDouble(String property) {
		return null;
	}

	@Override
	public Long getLong(String property) {
		return null;
	}

	@Override
	public List<String> getStringList(String property) {
		return Collections.emptyList();
	}

	@Override
	public IPluginConfig getGlobal() {
		return null;
	}

	@Override
	public Set<String> keySet() {
		return Collections.emptySet();
	}

	@Override
	public void addChangeListener(PluginConfigChangeListener listener) {

	}
}
