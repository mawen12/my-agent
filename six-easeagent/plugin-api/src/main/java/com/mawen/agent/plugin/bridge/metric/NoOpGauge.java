package com.mawen.agent.plugin.bridge.metric;

import com.mawen.agent.plugin.api.metric.Gauge;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpGauge implements Gauge<Object> {

	INSTANCE;

	@Override
	public Object getValue() {
		return null;
	}
}
