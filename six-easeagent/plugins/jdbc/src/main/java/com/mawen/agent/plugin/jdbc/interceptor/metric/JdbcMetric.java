package com.mawen.agent.plugin.jdbc.interceptor.metric;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nonnull;

import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.ContextUtils;
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
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.utils.ImmutableMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class JdbcMetric extends ServiceMetric implements RemovalListener<String, String> {
	private static final Logger log = LoggerFactory.getLogger(JdbcMetric.class);

	public static final ServiceMetricSupplier<JdbcMetric> METRIC_SUPPLIER = new ServiceMetricSupplier<JdbcMetric>() {
		@Override
		public NameFactory newNameFactory() {
			return JdbcMetric.nameFactory();
		}

		@Override
		public JdbcMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return new JdbcMetric(metricRegistry, nameFactory);
		}
	};

	public JdbcMetric(@Nonnull MetricRegistry metricRegistry, @Nonnull NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	public static Tags newConnectionTags() {
		Tags tags = new Tags("application", "jdbc-connection", "url");
		RedirectProcessor.setTagsIfRedirected(Redirect.DATABASE, tags);
		return tags;
	}

	public static Tags newStatementTags() {
		Tags tags = new Tags("application", "jdbc-statement", "signature");
		RedirectProcessor.setTagsIfRedirected(Redirect.DATABASE, tags);
		return tags;
	}

	public static NameFactory nameFactory() {
//		return NameFactory.createBuilder()
//				.timerType(MetricSubType.DEFAULT,
//						Maps.newHashMap().of(
//								MetricField.MIN_EXECUTION_TIME, MetricValueFetcher.SnapshotMinValue,
//								MetricField.MAX_EXECUTION_TIME, MetricValueFetcher.SnapshotMaxValue,
//								MetricField.MEAN_EXECUTION_TIME, MetricValueFetcher.SnapshotMeanValue,
//								MetricField.P25_EXECUTION_TIME, MetricValueFetcher.Snapshot25Percentile,
//								MetricField.P50_EXECUTION_TIME, MetricValueFetcher.Snapshot50Percentile,
//								MetricField.P75_EXECUTION_TIME, MetricValueFetcher.Snapshot75Percentile,
//								MetricField.P95_EXECUTION_TIME, MetricValueFetcher.Snapshot95Percentile,
//								MetricField.P98_EXECUTION_TIME, MetricValueFetcher.Snapshot98Percentile,
//								MetricField.P99_EXECUTION_TIME, MetricValueFetcher.Snapshot99Percentile,
//								MetricField.P999_EXECUTION_TIME, MetricValueFetcher.Snapshot999Percentile
//						))
//				.meterType(MetricSubType.DEFAULT,
//						Map.of(
//								MetricField.M1_RATE, MetricValueFetcher.MeteredM1RateIgnoreZero,
//								MetricField.M5_RATE, MetricValueFetcher.MeteredM5Rate,
//								MetricField.M15_RATE, MetricValueFetcher.MeteredM15Rate
//						))
//				.meterType(MetricSubType.ERROR,
//						Map.of(
//								MetricField.M1_ERROR_RATE, MetricValueFetcher.MeteredM1Rate,
//								MetricField.M5_ERROR_RATE, MetricValueFetcher.MeteredM5Rate,
//								MetricField.M15_ERROR_RATE, MetricValueFetcher.MeteredM15Rate
//						))
//				.counterType(MetricSubType.DEFAULT,
//						Map.of(
//								MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount
//						))
//				.counterType(MetricSubType.ERROR,
//						Map.of(
//								MetricField.EXECUTION_ERROR_COUNT, MetricValueFetcher.CountingCount
//						))
//				.build();
		return null;
	}

	public void collectMetric(String key, boolean success, Context context) {
		Timer timer = this.metricRegistry.timer(this.nameFactory.timerName(key, MetricSubType.DEFAULT));
		timer.update(Duration.ofMillis(ContextUtils.getDuration(context)));

		Counter counter = this.metricRegistry.counter(this.nameFactory.counterName(key, MetricSubType.DEFAULT));
		counter.inc();

		Meter meter = this.metricRegistry.meter(this.nameFactory.meterName(key, MetricSubType.DEFAULT));
		meter.mark();

		if (!success) {
			Counter errCounter = this.metricRegistry.counter(this.nameFactory.counterName(key, MetricSubType.ERROR));
			errCounter.inc();

			Meter errMeter = this.metricRegistry.meter(this.nameFactory.meterName(key, MetricSubType.ERROR));
			errMeter.mark();
		}
		// do not implement gauge
	}

	@Override
	public void onRemoval(RemovalNotification<String, String> notification) {
		try {
			String key = notification.getKey();
			List<String> list = Lists.newArrayList(
					Optional.ofNullable(this.nameFactory.counterName(key, MetricSubType.DEFAULT)).orElse(""),
					Optional.ofNullable(this.nameFactory.counterName(key, MetricSubType.ERROR)).orElse(""),
					Optional.ofNullable(this.nameFactory.meterName(key, MetricSubType.DEFAULT)).orElse(""),
					Optional.ofNullable(this.nameFactory.meterName(key, MetricSubType.ERROR)).orElse(""),
					Optional.ofNullable(this.nameFactory.timerName(key, MetricSubType.DEFAULT)).orElse(""),
					Optional.ofNullable(this.nameFactory.gaugeName(key, MetricSubType.DEFAULT)).orElse("")
			);

			list.forEach(metricRegistry::remove);
		}
		catch (Exception e) {
			log.warn("remove lru cache failed: " + e.getMessage());
		}
	}
}
