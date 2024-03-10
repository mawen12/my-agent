package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Gauge;


public record GaugeImpl(Gauge g) implements com.codahale.metrics.Gauge {

	public GaugeImpl(Gauge g) {
		this.g = Objects.requireNonNull(g, "g must not be null");
	}

	@Override
	public Object getValue() {
		return g.getValue();
	}
}
