package com.mawen.agent.metrics.impl;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.codahale.metrics.MetricRegistryListener;
import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.Timer;
import com.mawen.agent.plugin.bridge.metric.NoOpMetricsRegistry;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricRegistryImpl implements MetricRegistry {

	private final ConcurrentMap<String, Metric> metricCache;
	private final com.codahale.metrics.MetricRegistry metricRegistry;

	private MetricRegistryImpl(com.codahale.metrics.MetricRegistry metricRegistry) {
		this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry must not be null");
		this.metricCache = new ConcurrentHashMap<>();
		this.metricRegistry.addListener(new MetricRemoveListener());
	}

	public static MetricRegistry build(com.codahale.metrics.MetricRegistry metricRegistry) {
		return NoNull.of(new MetricRegistryImpl(metricRegistry), NoOpMetricsRegistry.INSTANCE);
	}

	@Override
	public boolean remove(String name) {
		synchronized (metricCache) {
			return metricRegistry.remove(name);
		}
	}

	@Override
	public Map<String, Metric> getMetrics() {
		return Collections.unmodifiableMap(metricCache);
	}

	@Override
	public Meter meter(String name) {
		return getOrAdd(name, MetricInstance.METER, metricRegistry -> MeterImpl.build(metricRegistry.meter(name)));
	}

	@Override
	public Counter counter(String name) {
		return getOrAdd(name, MetricInstance.COUNTER, metricRegistry -> CounterImpl.build(metricRegistry.counter(name)));
	}

//	@Override
//	public Gauge<?> gauge(String name, MetricSupplier<Gauge<?>> supplier) {
//		var metric = metricCache.get(name);
//		if (metric != null) {
//			return MetricInstance.GAUGE.to(name, metric);
//		}
//		synchronized (metricCache) {
//			metric = metricCache.get(name);
//			if (metric != null) {
//				return MetricInstance.GAUGE.to(name, metric);
//			}
//			var result = metricRegistry.gauge(name, new GaugeSupplier(supplier));
//			var g = ((GaugeImpl) result).g();
//			metricCache.putIfAbsent(name, g);
//			return g;
//		}
//	}

	@Override
	public Gauge<?> gauge(String name) {
		return getOrAdd(name, MetricInstance.GAUGE, metricRegistry -> GaugeImpl.build(metricRegistry.gauge(name)));
	}

	@Override
	public Histogram histogram(String name) {
		return getOrAdd(name, MetricInstance.HISTOGRAM, metricRegistry -> HistogramImpl.build(metricRegistry.histogram(name)));
	}

	@Override
	public Timer timer(String name) {
		return getOrAdd(name, MetricInstance.TIMER, metricRegistry -> TimerImpl.build(metricRegistry.timer(name)));
	}

	private <T extends Metric> T getOrAdd(String name, MetricInstance<T> instance, MetricBuilder<T> builder) {
		var metric = metricCache.get(name);
		if (metric != null) {
			return instance.to(name, metric);
		}
		synchronized (metricCache) {
			metric = metricCache.get(name);
			if (metric != null) {
				return instance.to(name, metric);
			}
			var t = builder.newMetric(metricRegistry);
			metricCache.putIfAbsent(name, t);
			return t;
		}
	}

//	public record GaugeSupplier(MetricSupplier<Gauge<?>> supplier) implements com.codahale.metrics.MetricRegistry.MetricSupplier<com.codahale.metrics.Gauge<?>> {
//
//		@Override
//		public com.codahale.metrics.Gauge<?> newMetric() {
//			var newGauge = supplier.newMetric();
//			return new GaugeImpl(newGauge);
//		}
//	}

	@FunctionalInterface
	private interface MetricBuilder<T extends Metric> {
		T newMetric(com.codahale.metrics.MetricRegistry metricRegistry);
	}

	class MetricRemoveListener implements MetricRegistryListener {

		@Override
		public void onGaugeAdded(String name, com.codahale.metrics.Gauge<?> gauge) {
			// ignored
		}

		@Override
		public void onGaugeRemoved(String name) {
			synchronized (metricCache) {
				metricCache.remove(name);
			}
		}

		@Override
		public void onCounterAdded(String name, com.codahale.metrics.Counter counter) {
			// ignored
		}

		@Override
		public void onCounterRemoved(String name) {
			synchronized (metricCache) {
				metricCache.remove(name);
			}
		}

		@Override
		public void onHistogramAdded(String name, com.codahale.metrics.Histogram histogram) {
			// ignored
		}

		@Override
		public void onHistogramRemoved(String name) {
			synchronized (metricCache) {
				metricCache.remove(name);
			}
		}

		@Override
		public void onMeterAdded(String name, com.codahale.metrics.Meter meter) {
			// ignored
		}

		@Override
		public void onMeterRemoved(String name) {
			synchronized (metricCache) {
				metricCache.remove(name);
			}
		}

		@Override
		public void onTimerAdded(String name, com.codahale.metrics.Timer timer) {
			// ignored
		}

		@Override
		public void onTimerRemoved(String name) {
			synchronized (metricCache) {
				metricCache.remove(name);
			}
		}
	}
}
