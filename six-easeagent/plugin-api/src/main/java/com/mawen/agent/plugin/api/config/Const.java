package com.mawen.agent.plugin.api.config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Const {
	int MAX_PLUGIN_STACK = 10000;
	String ENABLED_CONFIG = "enabled";

	int METRIC_DEFAULT_INTERVAL = 30;
	String METRIC_DEFAULT_INTERVAL_UNIT = "SECONDS";
	String METRIC_DEFAULT_TOPIC = "application-meter";

	String DEFAULT_APPEND_TYPE = "console";
}
