package com.mawen.agent.report.metric;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.report.plugin.ReporterRegistry;
import com.mawen.agent.report.sender.SenderWithEncoder;
import com.mawen.agent.report.util.Utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class MetricReporterFactoryImpl implements MetricReporterFactory, ConfigChangeListener {
	private static final Logger log = LoggerFactory.getLogger(MetricReporterFactoryImpl.class);

	private final ConcurrentHashMap<String, DefaultMetricReporter> reporters;
	private final Config reportConfig;

	public MetricReporterFactoryImpl(Config reportConfig) {
		this.reporters = new ConcurrentHashMap<>();
		this.reportConfig = reportConfig;
		this.reportConfig.addChangeListener(this);
	}

	public static MetricReporterFactory create(Config reportConfig) {
		return new MetricReporterFactoryImpl(reportConfig);
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		var changes = filterChanges(list);
		if (changes.isEmpty()) {
			return;
		}
		this.reportConfig.updateConfigs(changes);
	}

	@Override
	public Reporter reporter(IPluginConfig pluginConfig) {
		var reporter = reporters.get(pluginConfig.namespace());
		if (reporter != null) {
			return reporter;
		}

		synchronized (reporters) {
			reporter = reporters.get(pluginConfig.namespace());
			if (reporter != null) {
				return reporter;
			}
			reporter = new DefaultMetricReporter(pluginConfig, reportConfig);
			reporters.put(pluginConfig.namespace(), reporter);
			return reporter;
		}
	}

	private Map<String, String> filterChanges(List<ChangeItem> list) {
		return list.stream()
				.collect(Collectors.toMap(ChangeItem::fullName, ChangeItem::newValue));
	}

	public static class DefaultMetricReporter implements Reporter, ConfigChangeListener {

		private MetricProps metricProps;
		private SenderWithEncoder sender;
		private final IPluginConfig pluginConfig;
		private final Config reportConfig;
		private final Config metricConfig;

		public DefaultMetricReporter(IPluginConfig pluginConfig, Config reportConfig) {
			this.pluginConfig = pluginConfig;

			this.reportConfig = reportConfig;
			this.reportConfig.addChangeListener(this);

			this.metricProps = Utils.extractMetricProps(this.pluginConfig, reportConfig);
			this.metricConfig = this.metricProps.asReportConfig();

			this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.metricConfig);
		}

		@Override
		public void report(String context) {
			try {
				sender.send(new ByteWrapper(context.getBytes())).execute();
			}
			catch (IOException e) {
				log.warn("send error. {}", e.getMessage());
			}
		}

		@Override
		public void report(EncodedData encodedData) {
			try {
				sender.send(encodedData).execute();
			}
			catch (IOException e) {
				log.warn("send error. {}", e.getMessage());
			}
		}

		@Override
		public void onChange(List<ChangeItem> list) {
			if (list.isEmpty()) {
				return;
			}

			var senderName = this.metricProps.getSenderName();
			this.metricProps = Utils.extractMetricProps(pluginConfig, reportConfig);
			var changedConfig = this.metricProps.asReportConfig();

			this.metricConfig.updateConfigs(changedConfig.getConfigs());

			if (!metricProps.getSenderName().equals(senderName)) {
				this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.metricConfig);
			}
		}

		public MetricProps getMetricProps() {
			return metricProps;
		}

		public SenderWithEncoder getSender() {
			return sender;
		}

		public IPluginConfig getPluginConfig() {
			return pluginConfig;
		}

		public Config getReportConfig() {
			return reportConfig;
		}

		public Config getMetricConfig() {
			return metricConfig;
		}
	}
}
