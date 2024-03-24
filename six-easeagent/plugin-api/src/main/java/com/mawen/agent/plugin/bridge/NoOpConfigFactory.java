package com.mawen.agent.plugin.bridge;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.config.IConfigFactory;
import com.mawen.agent.plugin.api.config.IPluginConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public enum NoOpConfigFactory implements IConfigFactory {
	INSTANCE;

	@Override
	public Config getConfig() {
		return NoOpConfig.INSTANCE;
	}

	@Override
	public String getConfig(String property) {
		return "";
	}

	@Override
	public String getConfig(String property, String defaultValue) {
		return defaultValue;
	}

	@Override
	public IPluginConfig getConfig(String domain, String namespace, String name) {
		return new NoOpIPluginConfig(domain, namespace, name);
	}

	enum NoOpConfig implements Config {
		INSTANCE;

		@Override
		public boolean hasPath(String path) {
			return false;
		}

		@Override
		public String getString(String name) {
			return null;
		}

		@Override
		public String getString(String name, String defVal) {
			return defVal;
		}

		@Override
		public Integer getInt(String name) {
			return null;
		}

		@Override
		public Integer getInt(String name, int defVal) {
			return defVal;
		}

		@Override
		public Boolean getBoolean(String name) {
			return false;
		}

		@Override
		public Boolean getBoolean(String name, boolean defVal) {
			Boolean aBoolean = getBoolean(name);
			return aBoolean == null ? defVal : aBoolean;
		}

		@Override
		public Boolean getBooleanNullForUnset(String name) {
			return null;
		}

		@Override
		public Double getDouble(String name) {
			return 0.0;
		}

		@Override
		public Double getDouble(String name, double defVal) {
			return defVal;
		}

		@Override
		public Long getLong(String name) {
			return null;
		}

		@Override
		public Long getLong(String name, long defVal) {
			return defVal;
		}

		@Override
		public List<String> getStringList(String name) {
			return Collections.emptyList();
		}

		@Override
		public Set<String> keySet() {
			return Collections.emptySet();
		}

		@Override
		public Map<String, String> getConfigs() {
			return Collections.emptyMap();
		}
	}
}
