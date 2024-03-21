package com.mawen.agent.metrics;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import com.codahale.metrics.MetricRegistry;
import com.mawen.agent.metrics.config.MetricsConfig;
import com.mawen.agent.metrics.converter.Converter;
import com.mawen.agent.plugin.report.EncodedData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class AutoRefreshReporter implements Runnable {

	private final MetricsConfig config;
	private final Converter converter;
	private final Consumer<EncodedData> consumer;
	private final MetricRegistry registry;
	private AgentScheduledReporter reporter;

	public AutoRefreshReporter(MetricRegistry registry, MetricsConfig config, Converter converter, Consumer<EncodedData> consumer) {
		this.config = config;
		this.converter = converter;
		this.consumer = consumer;
		this.registry = registry;
		config.setIntervalChangeCallback(this);
	}

	@Override
	public synchronized void run() {
		if (reporter != null) {
			reporter.close();
			reporter = null;
		}
		reporter = AgentScheduledReporter.forRegistry(registry)
				.outputTo(consumer)
				.enabled(config::isEnabled)
				.convertRatesTo(TimeUnit.SECONDS)
				.convertDurationsTo(TimeUnit.MILLISECONDS)
				.build();
		reporter.setConverter(converter);
		reporter.start(config.getInterval(), config.getIntervalUnit());
	}

}
