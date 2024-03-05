package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Gauge;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@Getter
public class GaugeImpl implements com.codahale.metrics.Gauge {

	private final Gauge g;

	public GaugeImpl(Gauge g) {
		this.g = Objects.requireNonNull(g, "g must not be null");
	}

	@Override
	public Object getValue() {
		return g.getValue();
	}
}
