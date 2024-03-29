package com.mawen.agent.report.trace;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.mawen.agent.config.AutoRefreshConfigItem;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.async.trace.SDKAsyncReporter;
import com.mawen.agent.report.async.trace.TraceAsyncProps;
import com.mawen.agent.report.encoder.span.GlobalExtrasSupplier;
import com.mawen.agent.report.plugin.ReporterRegistry;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class TraceReport {

	private final RefreshableReporter<ReportSpan> spanRefreshableReporter;

	public TraceReport(Config reportConfig) {
		spanRefreshableReporter = initSpanRefreshableReporter(reportConfig);
		reportConfig.addChangeListener(new InternalListener());
	}

	public void report(ReportSpan span) {
		this.spanRefreshableReporter.report(span);
	}

	private RefreshableReporter<ReportSpan> initSpanRefreshableReporter(Config reportConfig) {
		var sender = ReporterRegistry.getSender(ReportConfigConst.TRACE_SENDER, reportConfig);

		var traceProperties = new TraceAsyncProps(reportConfig);

		var extrasSupplier = new GlobalExtrasSupplier() {

			Config gConfig = Agent.getConfig();
			AutoRefreshConfigItem<String> serviceName = new AutoRefreshConfigItem<>(gConfig, ConfigConst.SERVICE_NAME, Config::getString);
			AutoRefreshConfigItem<String> systemName = new AutoRefreshConfigItem<>(gConfig, ConfigConst.SYSTEM_NAME, Config::getString);
			@Override
			public String service() {
				return serviceName.getValue();
			}

			@Override
			public String system() {
				return systemName.getValue();
			}
		};

		var reporter = SDKAsyncReporter.builderSDKAsyncReporter(sender, traceProperties, extrasSupplier);

		reporter.startFlushThread();

		return new RefreshableReporter<>(reporter, reportConfig);
	}

	private class InternalListener implements ConfigChangeListener {

		@Override
		public void onChange(List<ChangeItem> list) {
			var cfg = filterChanges(list);

			if (cfg.isEmpty()) {
				return;
			}

			spanRefreshableReporter.refresh(cfg);
		}

		private Map<String, String> filterChanges(List<ChangeItem> list) {
			return list.stream().collect(Collectors.toMap(ChangeItem::fullName, ChangeItem::newValue));
		}
	}
}
