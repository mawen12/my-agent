package com.mawen.agent.plugin.bridge;

import java.io.OutputStream;
import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.api.metric.MetricSupplier;
import com.mawen.agent.plugin.api.metric.Snapshot;
import com.mawen.agent.plugin.api.metric.Timer;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public final class NoOpMetrics {
	public static final MetricRegistrySupplier NO_OP_METRIC_SUPPLIER = NoopMetricsRegistrySupplier.INSTANCE;
	public static final Gauge NO_OP_GAUGE = NoopGauge.INSTANCE;
	public static final Snapshot NO_OP_SNAPSHOT = NoopSnapshot.INSTANCE;
	public static final Timer NO_OP_TIMER = NoopTimer.INSTANCE;
	public static final Histogram NO_OP_HISTOGRAM = NoopHistogram.INSTANCE;
	public static final Counter NO_OP_COUNTER = NoopCounter.INSTANCE;
	public static final Meter NO_OP_METER = NoopMeter.INSTANCE;
	public static final MetricRegistry NO_OP_METRIC = NoopMetricsRegistry.INSTANCE;

	public static final class NoopMetricsRegistrySupplier implements MetricRegistrySupplier {
		private static final NoopMetricsRegistrySupplier INSTANCE = new NoopMetricsRegistrySupplier();

		@Override
		public MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
			return NoopMetricsRegistry.INSTANCE;
		}

		@Override
		public Reporter reporter(IPluginConfig config) {
			return NoOpReporter.INSTANCE;
		}
	}

	public enum NoopGauge implements Gauge<Object> {
		INSTANCE;

		@Override
		public Object getValue() {
			return null;
		}
	}

	public enum NoopSnapshot implements Snapshot {
		INSTANCE;
		private static final long[] EMPTY_LONG_ARRAY = new long[0];

		@Override
		public double getValue(double quantile) {
			return 0D;
		}

		@Override
		public long[] getValues() {
			return EMPTY_LONG_ARRAY;
		}

		@Override
		public int size() {
			return 0;
		}

		@Override
		public long getMax() {
			return 0L;
		}

		@Override
		public double getMean() {
			return 0D;
		}

		@Override
		public long getMin() {
			return 0L;
		}

		@Override
		public double getStdDev() {
			return 0D;
		}

		@Override
		public void dump(OutputStream output) {
			// NOP
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public static final class NoopTimer implements Timer {
		private static final NoopTimer INSTANCE = new NoopTimer();
		private static final Timer.Context CONTEXT = new NoopTimer.Context();

		@Override
		public void update(long duration, TimeUnit unit) {
			// NOP
		}

		@Override
		public void update(Duration duration) {
			// NOP
		}

		@Override
		public <T> T time(Callable<T> event) throws Exception {
			return event.call();
		}

		@Override
		public <T> T timeSupplier(Supplier<T> event) {
			return event.get();
		}

		@Override
		public void time(Runnable event) {
			// NOP
		}

		@Override
		public Timer.Context time() {
			return CONTEXT;
		}

		@Override
		public long getCount() {
			return 0L;
		}

		@Override
		public double getFifteenMinuteRate() {
			return 0D;
		}

		@Override
		public double getFiveMinuteRate() {
			return 0D;
		}

		@Override
		public double getMeanRate() {
			return 0D;
		}

		@Override
		public double getOneMinuteRate() {
			return 0D;
		}

		@Override
		public Snapshot getSnapshot() {
			return NoopSnapshot.INSTANCE;
		}

		@Override
		public Object unwrap() {
			return null;
		}

		private static class Context implements Timer.Context {
			@Override
			public long stop() {
				return 0L;
			}

			@Override
			public void close() {
				// NOP
			}
		}
	}

	public enum NoopHistogram implements Histogram {
		INSTANCE;

		@Override
		public void update(int value) {
			// NOP
		}

		@Override
		public void update(long value) {
			// NOP
		}

		@Override
		public long getCount() {
			return 0L;
		}

		@Override
		public Snapshot getSnapshot() {
			return NoopSnapshot.INSTANCE;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public enum NoopCounter implements Counter {
		INSTANCE;

		@Override
		public void inc() {
			// NOP
		}

		@Override
		public void inc(long n) {
			// NOP
		}

		@Override
		public void dec() {
			// NOP
		}

		@Override
		public void dec(long n) {
			// NOP
		}

		@Override
		public long getCount() {
			return 0L;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public enum NoopMeter implements Meter {
		INSTANCE;

		@Override
		public void mark() {
			// NOP
		}

		@Override
		public void mark(long n) {
			// NOP
		}

		@Override
		public long getCount() {
			return 0L;
		}

		@Override
		public double getFifteenMinuteRate() {
			return 0D;
		}

		@Override
		public double getFiveMinuteRate() {
			return 0D;
		}

		@Override
		public double getMeanRate() {
			return 0D;
		}

		@Override
		public double getOneMinuteRate() {
			return 0D;
		}

		@Override
		public Object unwrap() {
			return null;
		}
	}

	public enum NoopMetricsRegistry implements MetricRegistry {
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
			return NoopMeter.INSTANCE;
		}

		@Override
		public Counter counter(String name) {
			return NoopCounter.INSTANCE;
		}

		@Override
		public Gauge gauge(String name, MetricSupplier<Gauge> supplier) {
			return NoopGauge.INSTANCE;
		}

		@Override
		public Histogram histogram(String name) {
			return NoopHistogram.INSTANCE;
		}

		@Override
		public Timer timer(String name) {
			return NoopTimer.INSTANCE;
		}
	}
}
