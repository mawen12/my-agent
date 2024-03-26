package com.mawen.agent.metrics.converter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.metric.name.MetricName;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.SampleBuilder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class AgentPrometheusExports extends Collector implements Collector.Describable {

	private static final Logger LOGGER = LoggerFactory.getLogger(AgentPrometheusExports.class.getName());

	private final MetricRegistry metricRegistry;
	private final AbstractConverter abstractConverter;
	private final MetricFilter metricFilter = MetricFilter.ALL;
	private final SampleBuilder sampleBuilder;
	private final CounterExports counterExports = new CounterExports();
	private final MeterExports meterExports = new MeterExports();
	private final TimerExports timerExports = new TimerExports();
	private final HistogramExports histogramExports = new HistogramExports();
	private final GaugeExports gaugeExports = new GaugeExports();

	public AgentPrometheusExports(MetricRegistry metricRegistry, AbstractConverter abstractConverter, SampleBuilder sampleBuilder) {
		this.metricRegistry = metricRegistry;
		this.abstractConverter = abstractConverter;
		this.sampleBuilder = sampleBuilder;
	}

	@Override
	public List<MetricFamilySamples> collect() {
		Map<String, MetricFamilySamples> mfSampleMap = new HashMap<String, MetricFamilySamples>();
		gaugeExports.addToMap(mfSampleMap);
		counterExports.addToMap(mfSampleMap);
		meterExports.addToMap(mfSampleMap);
		timerExports.addToMap(mfSampleMap);
		histogramExports.addToMap(mfSampleMap);
		return new ArrayList<>(mfSampleMap.values());
	}

	@Override
	public List<MetricFamilySamples> describe() {
		return new ArrayList<>();
	}

	protected void addToMap(Map<String, MetricFamilySamples> mfSamplesMap, MetricFamilySamples newMfSamples) {
		if (newMfSamples != null) {
			MetricFamilySamples currentMfSamples = mfSamplesMap.get(newMfSamples.name);
			if (currentMfSamples == null) {
				mfSamplesMap.put(newMfSamples.name, newMfSamples);
			} else {
				List<MetricFamilySamples.Sample> samples = new ArrayList<>(currentMfSamples.samples);
				samples.addAll(newMfSamples.samples);
				mfSamplesMap.put(newMfSamples.name, new MetricFamilySamples(newMfSamples.name, currentMfSamples.type, currentMfSamples.help, samples));
			}
		}
	}

	private static String getHelpMessage(String metricName, Class<?> clazz) {
		return String.format("Generated from Dropwizard metric import (metric=%s, type=%s)", metricName, clazz.getName());
	}

	MetricFamilySamples.Sample doubleValue(String dropwizardName, Object obj, String valueType, Class<?> clazz) {
		double value;
		if (obj instanceof Number) {
			Number n = (Number) obj;
			value = n.doubleValue();
		} else {
			if (!(obj instanceof Boolean)) {
				LOGGER.warn("Invalid type for {} {}:{}", clazz.getSimpleName(), sanitizeMetricName(dropwizardName), obj == null ? "null" : clazz.getName());
				return null;
			}
			value = (Boolean)obj ? 1.0D : 0.0D;
		}

		return this.sampleBuilder.createSample(dropwizardName, "_" + valueType, Collections.emptyList(), Collections.emptyList(), value);
	}

	abstract class Exports<T extends Metric> {

		private final Collector.Type type;
		private final Class<?> clazz;

		public Exports(Type type, Class<?> clazz) {
			this.type = type;
			this.clazz = clazz;
		}

		public void addToMap(Map<String, MetricFamilySamples> map) {
			Map<String, Object> values = new HashMap<>();
			SortedMap<String, T> gaugeSortedMap = getMetric();
			for (String s : gaugeSortedMap.keySet()) {
				writeValue(MetricName.metricNameFor(s), gaugeSortedMap, values);
				for (Map.Entry<String, Object> entry : values.entrySet()) {
					MetricFamilySamples.Sample sample = doubleValue(s, entry.getValue(), entry.getKey(), clazz);
					AgentPrometheusExports.this.addToMap(map,
							new MetricFamilySamples(sample.name, type, getHelpMessage(sample.name, clazz),
									Collections.singletonList(sample)));
				}
				values.clear();
			}
		}

		protected abstract SortedMap<String, T> getMetric();

		protected abstract void writeValue(MetricName metricName, SortedMap<String, T> metric, Map<String, Object> values);
	}

	class CounterExports extends Exports<Counter> {

		public CounterExports() {
			super(Type.SUMMARY, Counter.class);
		}

		@Override
		protected SortedMap<String, Counter> getMetric() {
			return metricRegistry.getCounters(metricFilter);
		}

		@Override
		protected void writeValue(MetricName metricName, SortedMap<String, Counter> metric, Map<String, Object> values) {
			abstractConverter.writeCounters(metricName.key(), metricName.metricSubType(), metric, values);
		}
	}

	class MeterExports extends Exports<Meter> {

		public MeterExports() {
			super(Type.SUMMARY, Meter.class);
		}

		@Override
		protected SortedMap<String, Meter> getMetric() {
			return metricRegistry.getMeters(metricFilter);
		}

		@Override
		protected void writeValue(MetricName metricName, SortedMap<String, Meter> metric, Map<String, Object> values) {
			abstractConverter.writeMeters(metricName.key(), metricName.metricSubType(), metric, values);
		}
	}

	class TimerExports extends Exports<Timer> {

		public TimerExports() {
			super(Type.SUMMARY, Timer.class);
		}
		@Override
		protected SortedMap<String, Timer> getMetric() {
			return metricRegistry.getTimers(metricFilter);
		}

		@Override
		protected void writeValue(MetricName metricName, SortedMap<String, Timer> metric, Map<String, Object> values) {
			abstractConverter.writeTimers(metricName.key(), metricName.metricSubType(), metric, values);
		}
	}

	class HistogramExports extends Exports<Histogram> {

		public HistogramExports() {
			super(Type.SUMMARY, Histogram.class);
		}

		@Override

		protected SortedMap<String, Histogram> getMetric() {
			return metricRegistry.getHistograms(metricFilter);
		}

		@Override
		protected void writeValue(MetricName metricName, SortedMap<String, Histogram> metric, Map<String, Object> values) {
			abstractConverter.writeHistograms(metricName.key(), metricName.metricSubType(), metric, values);
		}
	}

	class GaugeExports extends Exports<Gauge> {

		public GaugeExports() {
			super(Type.GAUGE, Gauge.class);
		}

		@Override
		protected SortedMap<String, Gauge> getMetric() {
			return metricRegistry.getGauges(metricFilter);
		}

		@Override
		protected void writeValue(MetricName metricName, SortedMap<String, Gauge> metric, Map<String, Object> values) {
			abstractConverter.writeGauges(metricName.key(), metricName.metricSubType(), metric, values);
		}
	}
}
