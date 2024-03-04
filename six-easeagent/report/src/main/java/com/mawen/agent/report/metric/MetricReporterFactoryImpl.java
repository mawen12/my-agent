package com.mawen.agent.report.metric;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@Slf4j
public class MetricReporterFactoryImpl implements MetricReporterFactory, ConfigChangeListener {

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
		Map<String, String> changes = filterChanges(list);
		if (changes.isEmpty()) {
			return;
		}
		this.reportConfig.updateConfigs(changes);
	}

	@Override
	public Reporter reporter(IPluginConfig pluginConfig) {
		DefaultMetricReporter reporter = reporters.get(pluginConfig.namespace());
		if (reporter != null) {
			return reporter;
		}

		synchronized (reporter) {
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
		Map<String, String> cfg = new HashMap<>();
		list.forEach(one -> cfg.put(one.getFullName(),one.getNewValue()));

		return cfg;
	}

	@Getter
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

			String senderName = this.metricProps.getSenderName();
			this.metricProps = Utils.extractMetricProps(pluginConfig, reportConfig);
			Config changedConfig = this.metricProps.asReportConfig();

			this.metricConfig.updateConfigs(changedConfig.getConfigs());

			if (!metricProps.getSenderName().equals(senderName)) {
				this.sender = ReporterRegistry.getSender(this.metricProps.getSenderPrefix(), this.metricConfig);
			}
		}
	}
}