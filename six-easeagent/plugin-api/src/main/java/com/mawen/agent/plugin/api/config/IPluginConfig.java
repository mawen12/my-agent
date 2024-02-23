package com.mawen.agent.plugin.api.config;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface IPluginConfig {

	String domain();

	String namespace();

	String id();

	boolean hasProperty(String property);

	String getString(String property);

	Integer getInt(String property);

	Boolean getBoolean(String property);

	default boolean enabled() {
		Boolean b = getBoolean(Const.ENABLED_CONFIG);
		if (b == null) {
			return false;
		}
		return b;
	}

	default Boolean getBoolean(String property, Boolean defaultValue) {
		Boolean ret;
		if (!hasProperty(property)) {
			return defaultValue;
		}
		else {
			ret = getBoolean(property);
			return ret != null ? ret : defaultValue;
		}
	}

	Double getDouble(String property);

	Long getLong(String property);

	List<String> getStringList(String property);

	IPluginConfig getGlobal();

	Set<String> keySet();

	void addChangeListener(PluginConfigChangeListener listener);
}
