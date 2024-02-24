package com.mawen.agent.plugin.api.metric.name;

import java.util.function.Function;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.Snapshot;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum MetricValueFetcher {
	CountingCount(Counter::getCount, Counter.class),
	SnapshotMaxValue(Snapshot::getMax, Snapshot.class),
	SnapshotMeanValue(Snapshot::getMean, Snapshot.class),
	SnapshotMinValue(Snapshot::getMin, Snapshot.class),
	SnapshotMedianValue(Snapshot::getMedian, Snapshot.class),
	Snapshot25Percentile(s -> s.getValue(0.25), Snapshot.class),
	Snapshot50Percentile(Snapshot::getMedian, Snapshot.class),
	Snapshot75Percentile(Snapshot::get75thPercentile, Snapshot.class),
	Snapshot95Percentile(Snapshot::get95thPercentile, Snapshot.class),
	Snapshot98Percentile(Snapshot::get98thPercentile, Snapshot.class),
	Snapshot99Percentile(Snapshot::get99thPercentile, Snapshot.class),
	Snapshot999Percentile(Snapshot::get999thPercentile, Snapshot.class),
	MetricM1Rate(Meter::getOneMinuteRate, Meter.class),
	MetricM1RateIgnoreZero(Meter::getOneMinuteRate, Meter.class, aDouble -> aDouble),
	MetricM5Rate(Meter::getFiveMinuteRate, Meter.class),
	MetricM15Rate(Meter::getFifteenMinuteRate, Meter.class),
	MetricMeanRate(Meter::getMeanRate, Meter.class),
	MetricCount(Meter::getCount, Meter.class),
	;

	private final Function func;
	private final Class clazz;
	private final Function checker;

	public static <T, V> Function<T, V> wrapIgnoreZeroFunc(Function<T, V> origin) {
		return null;
	}

	<T, V> MetricValueFetcher(Function<T, V> function,  Class<T> clazz) {
		this(function, clazz, v -> v);
	}

	<T, V> MetricValueFetcher(Function<T, V> function, Class<T> clazz, Function<V, V> checker) {
		this.func = function;
		this.clazz = clazz;
		this.checker = checker;
	}

	public Function getFunc() {
		return func;
	}

	public Class getClazz() {
		return clazz;
	}

	public Function getChecker() {
		return checker;
	}

	public Object apply(Metric obj) {
		return checker.apply(func.apply(clazz.cast(obj)));
	}
}
