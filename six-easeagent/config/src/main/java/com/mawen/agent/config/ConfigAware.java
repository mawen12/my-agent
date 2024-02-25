package com.mawen.agent.config;

import com.mawen.agent.plugin.api.config.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public interface ConfigAware {
	void setConfig(Config config);
}
