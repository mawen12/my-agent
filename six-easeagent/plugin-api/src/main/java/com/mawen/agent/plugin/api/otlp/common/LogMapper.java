package com.mawen.agent.plugin.api.otlp.common;

import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.interceptor.MethodInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface LogMapper {
	String MDC_KEYS = "encoder.collectMDCKeys";

	AgentLogData mapLoggingEvent(MethodInfo methodInfo, int levelInt, IPluginConfig pluginConfig);
}
