package com.mawen.agent.plugin.api.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface PluginConfigChangeListener {

	void onChange(IPluginConfig oldConfig, IPluginConfig newConfig);
}
