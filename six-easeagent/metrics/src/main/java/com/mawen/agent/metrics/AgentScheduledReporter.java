package com.mawen.agent.metrics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricAttribute;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.ScheduledReporter;
import com.codahale.metrics.Timer;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigAdapter;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.metrics.converter.Converter;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.report.encoder.metric.MetricJsonEncoder;
import com.mawen.agent.report.plugin.ReporterRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class AgentScheduledReporter extends ScheduledReporter {

	private Converter converter;
	private final Consumer<EncodedData> dataConsumer;
	private final Supplier<Boolean> enabled;
	private final Encoder<Map<String, Object>> encoder;

	private AgentScheduledReporter(MetricRegistry registry,
			Consumer<EncodedData> dataConsumer,
			TimeUnit rateUnit,
			TimeUnit durationUnit,
			MetricFilter filter,
			ScheduledExecutorService executor,
			boolean shutdownExecutorOnStop,
			Set<MetricAttribute> disabledMetricAttributes,
			Supplier<Boolean> enabled,
			Converter converter) {
		super(registry, "logger-reporter", filter, rateUnit, durationUnit, executor, shutdownExecutorOnStop, disabledMetricAttributes);
		this.converter = converter;
		this.dataConsumer = dataConsumer;
		this.enabled = enabled;
		var reporterCfg = ReportConfigAdapter.extractReporterConfig(Agent.getConfig());
		var name = NoNull.of(reporterCfg.get(ReportConfigConst.METRIC_ENCODER), MetricJsonEncoder.ENCODER_NAME);
		this.encoder = ReporterRegistry.getEncoder(name);
		this.encoder.init(new Configs(reporterCfg));
	}

	public static Builder forRegistry(MetricRegistry registry) {
		return new Builder(registry);
	}

	@Override
	public void report(SortedMap<String, Gauge> gauges, SortedMap<String, Counter> counters, SortedMap<String, Histogram> histograms, SortedMap<String, Meter> meters, SortedMap<String, Timer> timers) {
		Boolean e = this.enabled.get();
		if (e == null || !e) {
			return;
		}

		List<Map<String, Object>> outputs = converter.convertMap(gauges, counters, histograms, meters, timers);
		for (Map<String, Object> output : outputs) {
			this.dataConsumer.accept(this.encoder.encode(output));
		}
	}

	@Override
	protected String getRateUnit() {
		return "events/" + super.getRateUnit();
	}

	public Converter getConverter() {
		return converter;
	}

	public void setConverter(Converter converter) {
		this.converter = converter;
	}

	public static class Builder {
		private final MetricRegistry registry;
		private TimeUnit rateUnit;
		private TimeUnit durationUnit;
		private MetricFilter filter;
		private ScheduledExecutorService executor;
		private boolean shutdownExecutorOnStop;
		private Set<MetricAttribute> disabledMetricAttributes;
		private Converter converter;
		private Supplier<Boolean> enabled;
		private Consumer<EncodedData> dataConsumer;

		private Builder(MetricRegistry registry) {
			this.registry = registry;
			this.rateUnit = TimeUnit.SECONDS;
			this.durationUnit = TimeUnit.MILLISECONDS;
			this.filter = MetricFilter.ALL;
			this.executor = null;
			this.shutdownExecutorOnStop = true;
			this.disabledMetricAttributes = Collections.emptySet();
		}

		public Builder shutdownExecutorOnStop(boolean shutdownExecutorOnStop) {
			this.shutdownExecutorOnStop = shutdownExecutorOnStop;
			return this;
		}

		public Builder scheduleOn(ScheduledExecutorService executor) {
			this.executor = executor;
			return this;
		}

		public Builder converter(Converter converter) {
			this.converter = converter;
			return this;
		}

		public Builder outputTo(Consumer<EncodedData> dataConsumer) {
			this.dataConsumer = dataConsumer;
			return this;
		}

		public Builder enabled(Supplier<Boolean> enabled) {
			this.enabled = enabled;
			return this;
		}

		public Builder convertRatesTo(TimeUnit rateUnit) {
			this.rateUnit = rateUnit;
			return this;
		}

		public Builder convertDurationsTo(TimeUnit durationUnit) {
			this.durationUnit = durationUnit;
			return this;
		}

		public Builder filter(MetricFilter filter) {
			this.filter = filter;
			return this;
		}

		public Builder disabledMetricAttributes(Set<MetricAttribute> disabledMetricAttributes) {
			this.disabledMetricAttributes = disabledMetricAttributes;
			return this;
		}

		public AgentScheduledReporter build() {
			return new AgentScheduledReporter(registry,
					dataConsumer,
					rateUnit, durationUnit,
					filter, executor, shutdownExecutorOnStop,
					disabledMetricAttributes, enabled, converter);
		}
	}
}
