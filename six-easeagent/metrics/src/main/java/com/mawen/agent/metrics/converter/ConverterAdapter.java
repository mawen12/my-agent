package com.mawen.agent.metrics.converter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.mawen.agent.metrics.impl.CounterImpl;
import com.mawen.agent.metrics.impl.MeterImpl;
import com.mawen.agent.metrics.impl.SnapshotImpl;
import com.mawen.agent.metrics.impl.TimerImpl;
import com.mawen.agent.plugin.api.metric.Metric;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.MetricName;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.MetricValueFetcher;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.tools.metrics.GaugeMetricModel;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class ConverterAdapter extends AbstractConverter {

	private final List<KeyType> keyTypes;
	private final NameFactory nameFactory;

	public ConverterAdapter(String category, String type, NameFactory metricNameFactory, KeyType keyType,
			Supplier<Map<String, Object>> attributes, String keyFieldName) {
		super(category, type, keyFieldName, attributes);
		this.keyTypes = Collections.singletonList(keyType);
		this.nameFactory = metricNameFactory;
	}

	public ConverterAdapter(NameFactory metricNameFactory, List<KeyType> keyTypes, Supplier<Map<String, Object>> attributes, Tags tags) {
		super(attributes, tags);
		this.keyTypes = Collections.unmodifiableList(keyTypes);
		this.nameFactory = metricNameFactory;
	}

	public ConverterAdapter(String category, String type, NameFactory metricNameFactory, KeyType keyType,
			Supplier<Map<String, Object>> attributes) {
		this(category, type, metricNameFactory, keyType, attributes, "resource");
	}

	@Override
	protected List<String> keysFromMetric(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters,
			SortedMap<String, Timer> timers) {
		var results = new HashSet<String>();
		for (var keyType : this.keyTypes) {
			if (keyType != null) {
				switch (keyType) {
					case Timer -> keys(timers.keySet(), results);
					case Histogram -> keys(histograms.keySet(), results);
					case Gauge -> keys(gauges.keySet(), results);
					case Counter -> keys(counters.keySet(), results);
					case Meter -> keys(meters.keySet(), results);
					default -> {}
				}
			}
		}
		return new ArrayList<>(results);
	}

	@Override
	protected void writeGauges(String key, MetricSubType metricSubType, SortedMap<String, Gauge> gauges, Map<String, Object> output) {
		var map = nameFactory.gaugeNames(key);
		consumerMetric(map, metricSubType, v -> {
			var gauge = gauges.get(v.name());
			if (gauge == null) {
				return;
			}
			var value = gauge.getValue();
			if (value instanceof GaugeMetricModel model) {
				output.putAll(model.toHashMap());
			}
			else if (value instanceof Number || value instanceof Boolean) {
				output.put("value", value);
			}
			else {
				output.put("value", value.toString());
			}
		});
	}

	@Override
	protected void writeCounters(String key, MetricSubType metricSubType, SortedMap<String, Counter> counters, Map<String, Object> output) {
		var map = nameFactory.counterNames(key);
		consumerMetric(map, metricSubType, v -> {
			Optional.ofNullable(counters.get(v.name()))
					.ifPresent(c -> v.valueFetcher().forEach((fieldName, fetcher) -> appendField(output, fieldName, fetcher, CounterImpl.build(c))));
		});
	}

	@Override
	protected void writeHistograms(String key, MetricSubType metricSubType, SortedMap<String, Histogram> histograms, Map<String, Object> output) {
		// write histograms, Temporarily unsupported
		// Please use timer to calculate the time of P95, P99, etc
	}

	@Override
	protected void writeMeters(String key, MetricSubType metricSubType, SortedMap<String, Meter> meters, Map<String, Object> output) {
		var map = nameFactory.meterNames(key);
		consumerMetric(map, metricSubType, v -> Optional
				.ofNullable(meters.get(v.name()))
				.ifPresent(m -> v.valueFetcher().forEach((fieldName, fetcher) -> appendField(output, fieldName, fetcher, MeterImpl.build(m)))));
	}

	@Override
	protected void writeTimers(String key, MetricSubType metricSubType, SortedMap<String, Timer> timers, Map<String, Object> output) {
		var map = nameFactory.timerNames(key);
		consumerMetric(map, metricSubType, v -> Optional
				.ofNullable(timers.get(v.name()))
				.ifPresent(t -> {
					final var snapshot = t.getSnapshot();
					v.valueFetcher().forEach((fieldName, fetcher) -> {
						if (fetcher.getClazz().equals(com.mawen.agent.plugin.api.metric.Snapshot.class)) {
							appendField(output, fieldName, fetcher, SnapshotImpl.build(snapshot));
						}
						else {
							appendField(output, fieldName, fetcher, TimerImpl.build(t));
						}
					});
				}));
	}

	protected static <T> void consumerMetric(Map<MetricSubType, T> map, MetricSubType metricSubType, Consumer<T> consumer) {
		if (metricSubType == null) {
			map.values().forEach(consumer);
		}
		T t = map.get(metricSubType);
		if (t != null) {
			consumer.accept(t);
		}
	}

	private void keys(Set<String> origins, Set<String> results) {
		origins.forEach(s -> results.add(MetricName.metricNameFor(s).key()));
	}

	private double convertDuration(Long duration) {
		return (double) duration / durationFactor;
	}

	private double convertDuration(Double duration) {
		return duration / durationFactor;
	}

	private double convertRate(Long rate) {
		return rate == null ? 0 : rate * rateFactor;
	}

	private double convertRate(double rate) {
		return rate * rateFactor;
	}

	private void appendField(Map<String, Object> output, MetricField fieldName, MetricValueFetcher fetcher, Metric metric) {
		switch (fieldName.getType()) {
			case DURATION -> appendDuration(output, fieldName.getField(), fetcher.apply(metric), fieldName.getScale());
			case RATE -> appendRate(output, fieldName.getField(), fetcher.apply(metric), fieldName.getScale());
			default -> output.put(fieldName.getField(), fetcher.apply(metric));
		}
	}

	private void appendRate(Map<String, Object> output, String key, Object value, int scale) {
		if (value instanceof Long l) {
			output.put(key, convertRate(l));
		}
		else if (value instanceof Double d) {
			output.put(key, toDouble(convertRate(d), scale));
		}
	}

	private void appendDuration(Map<String, Object> output, String key, Object value, int scale) {
		if (value instanceof Long l) {
			output.put(key, convertDuration(l));
		}
		else if (value instanceof Double d) {
			output.put(key, toDouble(convertDuration(d), scale));
		}
	}

	private double toDouble(Double i, int scale) {
		return BigDecimal.valueOf(i).setScale(scale, BigDecimal.ROUND_HALF_DOWN).doubleValue();
	}
}
