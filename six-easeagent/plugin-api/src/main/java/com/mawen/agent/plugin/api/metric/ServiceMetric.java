package com.mawen.agent.plugin.api.metric;

import java.util.HashMap;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.MetricValueFetcher;
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

	@Nonnull
	public static NameFactory nameFactory() {
		return NameFactory.createBuilder()
				.counterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.counterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.meterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero)
								.put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
								.put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
								.build())
				.meterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
								.put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
								.put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
								.build())
				.gaugeType(MetricSubType.DEFAULT, new HashMap<>())
				.timerType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue)
								.put(MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue)
								.put(MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue)
								.put(MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile)
								.put(MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50Percentile)
								.put(MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75Percentile)
								.put(MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95Percentile)
								.put(MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98Percentile)
								.put(MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99Percentile)
								.put(MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999Percentile)
								.build())
				.build();
	}
}
