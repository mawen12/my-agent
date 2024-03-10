package com.mawen.agent.plugin.elasticsearch.interceptor;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.MetricValueFetcher;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.tools.metrics.LastMinutesCounterGauge;
import com.mawen.agent.plugin.utils.ImmutableMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ElasticsearchMetric extends ServiceMetric {

	public ElasticsearchMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	public static NameFactory nameFactory() {
		return NameFactory.createBuilder()
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
				.gaugeType(MetricSubType.DEFAULT, new HashMap<>())
				.meterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_RATE, MetricValueFetcher.MeteredM1Rate)
								.put(MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate)
								.put(MetricField.MEAN_RATE, MetricValueFetcher.MeteredMeanRate)
								.build())
				.meterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate)
								.put(MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate)
								.put(MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate)
								.build())
				.counterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.counterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount)
								.build())
				.build();
	}

	public void collectMetric(String key, long duration, boolean success) {
		metricRegistry.timer(this.nameFactory.timerName(key, MetricSubType.DEFAULT)).update(duration, TimeUnit.MILLISECONDS);
		final var defaultMeter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.DEFAULT));
		final var defaultCounter = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.DEFAULT));

		if (!success) {
			final var errorMeter = metricRegistry.meter(nameFactory.meterName(key, MetricSubType.ERROR));
			final var errorCount = metricRegistry.counter(nameFactory.counterName(key, MetricSubType.ERROR));
			errorMeter.mark();
			errorCount.inc();
		}
		defaultMeter.mark();
		defaultCounter.inc();

		var gaugeName = nameFactory.gaugeNames(key).get(MetricSubType.DEFAULT);
		metricRegistry.gauge(gaugeName.name(), () -> () ->
				new LastMinutesCounterGauge((long) (defaultMeter.getOneMinuteRate() * 60),
						(long) (defaultMeter.getFiveMinuteRate() * 60 * 5),
						(long) (defaultMeter.getFifteenMinuteRate() * 60 * 15),
						""));
	}
}
