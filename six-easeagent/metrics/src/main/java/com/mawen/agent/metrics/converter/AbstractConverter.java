package com.mawen.agent.metrics.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.Tags;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public abstract class AbstractConverter implements Converter {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConverter.class);

	private final String rateUnit;
	private final String durationUnit;
	final long durationFactor;
	final Long rateFactor;
	private final Tags tags;
	private final Supplier<Map<String, Object>> additionalAttributes;

	AbstractConverter(String category, String type, String keyFieldName, Supplier<Map<String, Object>> additionalAttributes) {
		this(additionalAttributes, new Tags(category, type, keyFieldName));
	}

	AbstractConverter(Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
		this.rateFactor = TimeUnit.SECONDS.toSeconds(1);
		this.rateUnit = calculateRateUnit();
		this.durationFactor = TimeUnit.MILLISECONDS.toNanos(1);
		this.durationUnit = TimeUnit.MILLISECONDS.toString().toLowerCase(Locale.US);
		this.additionalAttributes = additionalAttributes;
		this.tags = tags;
	}

	public List<Map<String, Object>> convertMap(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters,
			SortedMap<String, Timer> timers) {

		var keys = keysFromMetric(gauges, counters, histograms, meters, timers);
		final var result = new ArrayList<Map<String, Object>>();
		for (String key : keys) {
			try {
				var output = buildMap();
				writeKey(output, key);
				writeTag(output);
				writeGauges(key, null, gauges, output);
				writeCounters(key, null, counters, output);
				writeHistograms(key, null, histograms, output);
				writeMeters(key, null, meters, output);
				writeTimers(key, null, timers, output);
				result.add(output);
			}
			catch (Exception e) {
				LOGGER.trace("convert key of " + key + " error: " + e.getMessage());
			}
		}
		return result;
	}

	protected abstract List<String> keysFromMetric(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters,
			SortedMap<String, Timer> timers);

	protected abstract void writeGauges(String key, MetricSubType metricSubType, SortedMap<String, Gauge> gauges, Map<String, Object> output);

	protected abstract void writeCounters(String key, MetricSubType metricSubType, SortedMap<String, Counter> counters, Map<String, Object> output);

	protected abstract void writeHistograms(String key, MetricSubType metricSubType, SortedMap<String, Histogram> histograms, Map<String, Object> output);

	protected abstract void writeMeters(String key, MetricSubType metricSubType, SortedMap<String, Meter> meters, Map<String, Object> output);

	protected abstract void writeTimers(String key, MetricSubType metricSubType, SortedMap<String, Timer> timers, Map<String, Object> output);

	private String calculateRateUnit() {
		final var s = TimeUnit.SECONDS.toString().toLowerCase(Locale.US);
		return s.substring(0, s.length() - 1);
	}

	private Map<String, Object> buildMap() {
		var map = new HashMap<String, Object>();
		map.put("timestamp", System.currentTimeMillis());
		map.putAll(additionalAttributes.get());
		return map;
	}

	private void writeTag(Map<String, Object> output) {
		output.put(Tags.CATEGORY, tags.getCategory());
		output.put(Tags.TYPE, tags.getType());
		output.putAll(tags.getTags());
	}

	private void writeKey(Map<String, Object> output, String key) {
		output.put(tags.getKeyFieldName(), key);
	}
}
