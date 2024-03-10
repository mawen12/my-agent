package com.mawen.agent.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mawen.agent.config.ConfigAware;
import com.mawen.agent.metrics.config.PluginMetricsConfig;
import com.mawen.agent.metrics.converter.ConverterAdapter;
import com.mawen.agent.metrics.converter.KeyType;
import com.mawen.agent.metrics.converter.MetricsAdditionalAttributes;
import com.mawen.agent.metrics.impl.MetricRegistryImpl;
import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.MetricProvider;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.report.AgentReportAware;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@Getter
public class MetricProviderImpl implements AgentReportAware, ConfigAware, MetricProvider {

	private Config config;
	private final List<MetricRegistry> registries = new ArrayList<>();
	private final List<AutoRefreshReporter> reporters = new ArrayList<>();
	private AgentReport agentReport;
	private Supplier<Map<String, Object>> additionalAttributes;

	@Override
	public void setConfig(Config config) {
		this.config = config;
		this.additionalAttributes = new MetricsAdditionalAttributes(config);
	}

	@Override
	public MetricRegistrySupplier metricSupplier() {
		return new ApplicationMetricRegistrySupplier();
	}

	@Override
	public void setAgentReport(AgentReport report) {
		this.agentReport = report;
	}

	public void registerMetricRegistry(MetricRegistry registry) {
		synchronized (registries) {
			registries.add(registry);
		}
	}

	public void registerReporter(AutoRefreshReporter reporter) {
		synchronized (reporters) {
			reporters.add(reporter);
		}
	}

	public static List<KeyType> keyTypes(NameFactory nameFactory) {
		var keyTypes = new ArrayList<KeyType>();
		for (var metricType : nameFactory.metricTypes()) {
			switch (metricType) {
				case TimerType -> keyTypes.add(KeyType.Timer);
				case GaugeType -> keyTypes.add(KeyType.Gauge);
				case CounterType -> keyTypes.add(KeyType.Counter);
				case HistogramType -> keyTypes.add(KeyType.Histogram);
				case MeterType -> keyTypes.add(KeyType.Meter);
			}
		}
		return keyTypes;
	}

	public class ApplicationMetricRegistrySupplier implements MetricRegistrySupplier {

		@Override
		public MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
			var metricsConfig = new PluginMetricsConfig(config);
			var keyTypes = keyTypes(nameFactory);
			var converterAdapter = new ConverterAdapter(nameFactory, keyTypes, additionalAttributes, tags);
			var reporter = agentReport.metricReporter().reporter(config);
			var metricRegistry = MetricRegistryService.DEFAULT.createMetricRegistry(converterAdapter, additionalAttributes, tags);
			var autoRefreshReporter = new AutoRefreshReporter(metricRegistry, metricsConfig, converterAdapter, reporter::report);
			autoRefreshReporter.run();
			registerReporter(autoRefreshReporter);

			var result = MetricRegistryImpl.build(metricRegistry);
			registerMetricRegistry(result);
			return result;
		}

		@Override
		public Reporter reporter(IPluginConfig config) {
			return agentReport.metricReporter().reporter(config);
		}
	}
}
