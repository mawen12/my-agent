package com.mawen.agent.plugin.bridge.metric;

import java.util.Collections;
import java.util.Map;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.Timer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpMetricsRegistry implements MetricRegistry {

	INSTANCE;

	@Override
	public boolean remove(String name) {
		return true;
	}

	@Override
	public Map<String, Metric> getMetrics() {
		return Collections.emptyMap();
	}

	@Override
	public Meter meter(String name) {
		return NoOpMeter.INSTANCE;
	}

	@Override
	public Counter counter(String name) {
		return NoOpCounter.INSTANCE;
	}

	@Override
	public Gauge<?> gauge(String name) {
		return NoOpGauge.INSTANCE;
	}

	@Override
	public Histogram histogram(String name) {
		return NoOpHistogram.INSTANCE;
	}

	@Override
	public Timer timer(String name) {
		return NoOpTimer.INSTANCE;
	}
}
