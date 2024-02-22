package com.mawen.agent.plugin.enums;

import com.mawen.agent.plugin.api.config.ConfigConst.PluginID;

/**
 * Priority definition, lower value with higher priority.
 * Higher priority interceptor run enter before lower ones,
 * but exit after lower priority interceptors.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public enum Order {
	FOUNDATION(0, "foundation"),
	HIGHEST(10, "highest"),
	REDIRECT(19, PluginID.REDIRECT),
	HIGH(20, "high"),
	FORWARDED(30, PluginID.FORWARDED),

	TRACING_INIT(90, PluginID.TRACING),
	TRACING(100, PluginID.TRACING),

	METRIC(200, PluginID.METRIC),
	LOG(201, PluginID.LOG),
	LOW(210, "low"),
	LOWEST(255, "lowest");

	private final int value;
	private final String name;

	Order(int value, String name) {
		this.value = value;
		this.name = name;
	}

	public int getOrder() {
		return value;
	}

	public String getName() {
		return name;
	}
}
