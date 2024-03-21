package com.mawen.agent.metrics.impl;

import java.lang.reflect.ParameterizedType;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.Timer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public abstract class MetricInstance<T extends Metric> {

	public static final MetricInstance<Counter> COUNTER = new MetricInstance<>() {
		@Override
		protected Counter toInstance(String name, Metric metric) {
			return (Counter) metric;
		}
	};

	public static final MetricInstance<Histogram> HISTOGRAM = new MetricInstance<>() {
		@Override
		protected Histogram toInstance(String name, Metric metric) {
			return (Histogram) metric;
		}
	};

	public static final MetricInstance<Meter> METER = new MetricInstance<>() {
		@Override
		protected Meter toInstance(String name, Metric metric) {
			return (Meter) metric;
		}
	};

	public static final MetricInstance<Timer> TIMER = new MetricInstance<>() {
		@Override
		protected Timer toInstance(String name, Metric metric) {
			return (Timer) metric;
		}
	};

	public static final MetricInstance<Gauge> GAUGE = new MetricInstance<>() {
		@Override
		protected Gauge toInstance(String name, Metric metric) {
			return (Gauge) metric;
		}
	};

	private final Class<?> type;

	private MetricInstance() {
		var superclass = getClass().getGenericSuperclass();
		if (superclass instanceof Class<?>) {
			throw new IllegalArgumentException("Internal error: MetricInstance constructed without actual type information");
		}
		var t = ((ParameterizedType) superclass).getActualTypeArguments()[0];
		if (!(t instanceof Class<?> clazz)) {
			throw new IllegalArgumentException();
		}
		type = clazz;
	}

	protected T to(String name, Metric metric) {
		if (!type.isInstance(metric)) {
			throw new IllegalArgumentException(
					String.format("%s is already used for a different type<%s> of metric", name, metric.getClass().getName()));
		}
		return toInstance(name, metric);
	}

	protected abstract T toInstance(String name, Metric metric);
}
