package com.mawen.agent.plugin.tools.metrics;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.Timer;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.MetricValueFetcher;
import com.mawen.agent.plugin.api.metric.name.NameFactory;

import static com.mawen.agent.plugin.api.metric.name.MetricField.*;
import static com.mawen.agent.plugin.api.metric.name.MetricValueFetcher.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ServerMetric extends ServiceMetric {

	public static final ServiceMetricSupplier<ServerMetric> SERVICE_METRIC_SUPPLIER = new ServiceMetricSupplier<ServerMetric>() {
		@Override
		public NameFactory newNameFactory() {
			return ServiceMetric.nameFactory();
		}

		@Override
		public ServerMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return null;
		}
	};

	public ServerMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	public void collectMetric(String key, int statusCode, Throwable throwable, long startMillis, long endMillis) {
		Timer timer = timer(key, MetricSubType.DEFAULT);
		timer.update(Duration.ofMillis(endMillis - startMillis));
		final Meter defaultMeter = meter(key, MetricSubType.DEFAULT);
		final Meter errorMeter = meter(key, MetricSubType.ERROR);
		Counter defaultCounter = counter(key, MetricSubType.DEFAULT);
		Counter errorCounter = counter(key, MetricSubType.ERROR);
		boolean hasException = throwable != null;
		if (statusCode >= 400 || hasException) {
			errorMeter.mark();
			errorCounter.inc();
		}
		defaultMeter.mark();
		defaultCounter.inc();

		gauge(key, MetricSubType.DEFAULT, () -> () -> {
			BigDecimal m1ErrorPercent = BigDecimal.ZERO;
			BigDecimal m5ErrorPercent = BigDecimal.ZERO;
			BigDecimal m15ErrorPercent = BigDecimal.ZERO;
			BigDecimal error = BigDecimal.valueOf(errorMeter.getOneMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
			BigDecimal n = BigDecimal.valueOf(defaultMeter.getOneMinuteRate());
			// 1 minute
			if (n.compareTo(BigDecimal.ZERO) != 0) {
				m1ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
			}
			// 5 minute
			error = BigDecimal.valueOf(errorMeter.getFiveMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
			n = BigDecimal.valueOf(defaultMeter.getFiveMinuteRate());
			if (n.compareTo(BigDecimal.ZERO) != 0) {
				m5ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
			}
			// 15 minute
			error = BigDecimal.valueOf(errorMeter.getFifteenMinuteRate()).setScale(5, BigDecimal.ROUND_HALF_DOWN);
			n = BigDecimal.valueOf(defaultMeter.getFifteenMinuteRate());
			if (n.compareTo(BigDecimal.ZERO) != 0) {
				m15ErrorPercent = error.divide(n, 2, BigDecimal.ROUND_HALF_UP);
			}
			return new ErrorPercentModelGauge(m1ErrorPercent, m5ErrorPercent, m15ErrorPercent);
		});
	}

	@Nonnull
	public static NameFactory nameFactory() {
		return NameFactory.createBuilder()
				.counterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(EXECUTION_COUNT, CountingCount)
								.build())
				.counterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(EXECUTION_ERROR_COUNT, CountingCount)
								.build())
				.meterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(M1_RATE, MeteredM1RateIgnoreZero)
								.put(M5_RATE, MeteredM5Rate)
								.put(M15_RATE, MeteredM15Rate)
								.put(MEAN_RATE, MeteredMeanRate)
								.build())
				.meterType(MetricSubType.ERROR,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(M1_ERROR_RATE, MeteredM1RateIgnoreZero)
								.put(M5_ERROR_RATE, MeteredM5Rate)
								.put(M15_ERROR_RATE, MeteredM15Rate)
								.put(MEAN_RATE, MeteredMeanRate)
								.build())
				.gaugeType(MetricSubType.DEFAULT, new HashMap<>())
				.timerType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MIN_EXECUTION_TIME, SnapshotMinValue)
								.put(MAX_EXECUTION_TIME, SnapshotMaxValue)
								.put(MEAN_EXECUTION_TIME, SnapshotMeanValue)
								.put(P25_EXECUTION_TIME, Snapshot25Percentile)
								.put(P50_EXECUTION_TIME, Snapshot50Percentile)
								.put(P75_EXECUTION_TIME, Snapshot75Percentile)
								.put(P95_EXECUTION_TIME, Snapshot95Percentile)
								.put(P98_EXECUTION_TIME, Snapshot98Percentile)
								.put(P99_EXECUTION_TIME, Snapshot99Percentile)
								.put(P999_EXECUTION_TIME, Snapshot999Percentile)
								.build())
				.build();
	}

}
