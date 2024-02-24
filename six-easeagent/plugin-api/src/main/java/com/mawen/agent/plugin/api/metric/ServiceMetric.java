package com.mawen.agent.plugin.api.metric;

import javax.annotation.Nonnull;

import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.NameFactory;

/**
 * a base Service Metric
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public abstract class ServiceMetric {
	protected final MetricRegistry metricRegistry;
	protected final NameFactory nameFactory;

	public ServiceMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
		this.metricRegistry = metricRegistry;
		this.nameFactory = nameFactory;
	}

	public Meter meter(String key, MetricSubType subType) {
		return metricRegistry.meter(nameFactory.meterName(key, subType));
	}

	public Counter counter(String key, MetricSubType subType) {
		return metricRegistry.counter(nameFactory.counterName(key, subType));
	}

	public Timer timer(String key, MetricSubType subType) {
		return metricRegistry.timer(nameFactory.timerName(key, subType));
	}

	public Histogram histogram(String key, MetricSubType subType) {
		return metricRegistry.histogram(nameFactory.histogramName(key, subType));
	}

	public Gauge gauge(String key, MetricSubType subType, MetricSupplier<Gauge> supplier) {
		return metricRegistry.gauge(nameFactory.gaugeName(key, subType), supplier);
	}

	public MetricRegistry getMetricRegistry() {
		return metricRegistry;
	}

	public NameFactory getNameFactory() {
		return nameFactory;
	}
}
