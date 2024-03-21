package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.bridge.metric.NoOpGauge;
import com.mawen.agent.plugin.utils.NoNull;


public class GaugeImpl implements Gauge {

	private final com.codahale.metrics.Gauge<?> gauge;

	private GaugeImpl(com.codahale.metrics.Gauge<?> g) {
		this.gauge = Objects.requireNonNull(g, "g must not be null");
	}

	public static Gauge build(com.codahale.metrics.Gauge<?> g) {
		return NoNull.of(new GaugeImpl(g), NoOpGauge.INSTANCE);
	}

	@Override
	public Object getValue() {
		return gauge.getValue();
	}
}
