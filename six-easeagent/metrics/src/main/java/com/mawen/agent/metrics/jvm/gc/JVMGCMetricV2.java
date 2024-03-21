package com.mawen.agent.metrics.jvm.gc;

import java.lang.management.ManagementFactory;

import javax.management.NotificationEmitter;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.MetricValueFetcher;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.utils.ImmutableMap;
import com.sun.management.GarbageCollectionNotificationInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class JVMGCMetricV2 extends ServiceMetric {

	private static final String NO_GC = "No GC";

	public static final ServiceMetricSupplier<JVMGCMetricV2> METRIC_SUPPLIER = new ServiceMetricSupplier<>() {
		@Override
		public NameFactory newNameFactory() {
			return JVMGCMetricV2.nameFactory();
		}

		@Override
		public JVMGCMetricV2 newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return new JVMGCMetricV2(metricRegistry, nameFactory);
		}
	};

	private static IPluginConfig config;

	public JVMGCMetricV2(MetricRegistry metricRegistry, NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	public static JVMGCMetricV2 getMetric() {
		config = AutoRefreshPluginConfigRegistry.getOrCreate("observability", "jvmGc", "metric");
		Tags tags = new Tags("application", "jvm-gc", "resource");

		JVMGCMetricV2 v2 = ServiceMetricRegistry.getOrCreate(config, tags, METRIC_SUPPLIER);
		v2.collect();

		return v2;
	}

	 public static NameFactory nameFactory() {
		return NameFactory.createBuilder()
				.meterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.TIMES, MetricValueFetcher.MeteredCount)
								.put(MetricField.TIMES_RATE, MetricValueFetcher.MeteredMeanRate)
								.build())
				.counterType(MetricSubType.DEFAULT,
						ImmutableMap.<MetricField, MetricValueFetcher>builder()
								.put(MetricField.TOTAL_COLLECTION_TIME, MetricValueFetcher.CountingCount)
								.build())
				.build();
	}

	public void collect() {
		for (var mBean : ManagementFactory.getGarbageCollectorMXBeans()) {
			if (!(mBean instanceof NotificationEmitter notificationEmitter)) {
				continue;
			}

			var listener = getListener();
			notificationEmitter.addNotificationListener(listener, null, null);
		}
	}

	private NotificationListener getListener() {
		return (notification, ref) -> {
			if (!notification.getType().equals(GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION)) {
				return;
			}

			if (!config.enabled()) {
				return;
			}

			var compositeData = (CompositeData) notification.getUserData();
			var notificationInfo = GarbageCollectionNotificationInfo.from(compositeData);
			var gcCause = notificationInfo.getGcCause();
			var gcInfo = notificationInfo.getGcInfo();
			var duration = gcInfo.getDuration();

			var gcName = notificationInfo.getGcName();
			var meterNames = nameFactory.meterNames(gcName);
			meterNames.forEach((type, name) -> {
				var meter = metricRegistry.meter(name.name());
				if (!NO_GC.equals(gcCause)) {
					meter.mark();
				}
			});

			var counterNames = nameFactory.counterNames(gcName);
			counterNames.forEach((type, name) -> {
				var counter = metricRegistry.counter(name.name());
				if (!NO_GC.equals(gcCause)) {
					counter.inc(duration);
				}
			});
		};

	}

}
