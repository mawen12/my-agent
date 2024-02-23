package com.mawen.agent.plugin.api.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface IConfigFactory {

	/**
	 * Returns the global configuration of this Java agent.
	 *
	 * @return The global configuration of this Java agent
	 */
	Config getConfig();

	/**
	 * Returns a configuration property from the agent's all configuration
	 *
	 * @return The configuration of this Java agent.
	 */
	String getConfig(String property);

	String getConfig(String property, String defaultValue);

	/**
	 * Returns the agent's plugin configuration.
	 *
	 * @return The configuration of a special plugin of Java agent.
	 */
	IPluginConfig getConfig(String domain, String namespace, String name);
}
