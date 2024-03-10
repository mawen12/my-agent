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
import com.mawen.agent.plugin.api.metric.MetricSupplier;
import com.mawen.agent.plugin.api.metric.Timer;
import com.mawen.agent.plugin.bridge.NoOpMetrics;
import com.mawen.agent.plugin.utils.NoNull;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricRegistryImpl implements MetricRegistry {

	private final ConcurrentMap<String, Metric> metricCache;
	@Getter
	private final com.codahale.metrics.MetricRegistry metricRegistry;

	MetricBuilder<Counter> counters = new MetricBuilder<>() {
		@Override
		public Counter newMetric(String name) {
			return NoNull.of(CounterImpl.build(metricRegistry.counter(name)), NoOpMetrics.NO_OP_COUNTER);
		}
	};

	MetricBuilder<Histogram> histograms = new MetricBuilder<>() {
		@Override
		public Histogram newMetric(String name) {
			return NoNull.of(HistogramImpl.build(metricRegistry.histogram(name)), NoOpMetrics.NO_OP_HISTOGRAM);
		}
	};

	MetricBuilder<Meter> meters = new MetricBuilder<>() {
		@Override
		public Meter newMetric(String name) {
			return NoNull.of(MeterImpl.build(metricRegistry.meter(name)), NoOpMetrics.NO_OP_METER);
		}
	};

	MetricBuilder<Timer> timers = new MetricBuilder<>() {
		@Override
		public Timer newMetric(String name) {
			return NoNull.of(TimerImpl.build(metricRegistry.timer(name)), NoOpMetrics.NO_OP_TIMER);
		}
	};

	private MetricRegistryImpl(com.codahale.metrics.MetricRegistry metricRegistry) {
		this.metricRegistry = Objects.requireNonNull(metricRegistry, "metricRegistry must not be null");
		this.metricCache = new ConcurrentHashMap<>();
		this.metricRegistry.addListener(new MetricRemoveListener());
	}

	public static MetricRegistry build(com.codahale.metrics.MetricRegistry metricRegistry) {
		return metricRegistry == null ? NoOpMetrics.NO_OP_METRIC : new MetricRegistryImpl(metricRegistry);
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
		return getOrAdd(name, MetricInstance.METER, meters);
	}

	@Override
	public Counter counter(String name) {
		return getOrAdd(name, MetricInstance.COUNTER, counters);
	}

	@Override
	public Gauge gauge(String name, MetricSupplier<Gauge> supplier) {
		var metric = metricCache.get(name);
		if (metric != null) {
			return MetricInstance.GAUGE.to(name, metric);
		}
		synchronized (metricCache) {
			metric = metricCache.get(name);
			if (metric != null) {
				return MetricInstance.GAUGE.to(name, metric);
			}
			var result = metricRegistry.gauge(name, new GaugeSupplier(supplier));
			var g = ((GaugeImpl) result).g();
			metricCache.putIfAbsent(name, g);
			return g;
		}
	}

	@Override
	public Histogram histogram(String name) {
		return getOrAdd(name, MetricInstance.HISTOGRAM, histograms);
	}

	@Override
	public Timer timer(String name) {
		return getOrAdd(name, MetricInstance.TIMER, timers);
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
			var t = builder.newMetric(name);
			metricCache.putIfAbsent(name, t);
			return t;
		}
	}

	public static class GaugeSupplier implements com.codahale.metrics.MetricRegistry.MetricSupplier<com.codahale.metrics.Gauge> {

		private final MetricSupplier<Gauge> supplier;

		public GaugeSupplier(MetricSupplier<Gauge> supplier) {
			this.supplier = supplier;
		}

		@Override
		public com.codahale.metrics.Gauge newMetric() {
			var newGauge = supplier.newMetric();
			return new GaugeImpl(newGauge);
		}
	}

	private interface MetricBuilder<T extends Metric> {
		T newMetric(String name);
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
