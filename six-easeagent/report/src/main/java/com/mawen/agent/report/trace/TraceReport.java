package com.mawen.agent.report.trace;

import com.mawen.agent.config.AutoRefreshConfigItem;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.async.trace.SDKAsyncReporter;
import com.mawen.agent.report.async.trace.TraceAsyncProps;
import com.mawen.agent.report.encoder.span.GlobalExtrasSupplier;
import com.mawen.agent.report.plugin.ReporterRegistry;
import com.mawen.agent.report.sender.SenderWithEncoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class TraceReport {

	private final RefreshableReporter<ReportSpan> spanRefreshableReporter;

	public TraceReport(Config reportConfig) {
		spanRefreshableReporter = initSpanRefreshableReporter(reportConfig);
	}

	public void report(ReportSpan span) {
		this.spanRefreshableReporter.report(span);
	}

	private RefreshableReporter<ReportSpan> initSpanRefreshableReporter(Config reportConfig) {
		SenderWithEncoder sender = ReporterRegistry.getSender(ReportConfigConst.TRACE_SENDER, reportConfig);

		TraceAsyncProps traceProperties = new TraceAsyncProps(reportConfig);

		GlobalExtrasSupplier extrasSupplier = new GlobalExtrasSupplier() {

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

		SDKAsyncReporter<ReportSpan> reporter = SDKAsyncReporter.builderSDKAsyncReporter(sender, traceProperties, extrasSupplier);

		reporter.startFlushThread();

		return new RefreshableReporter<>(reporter, reportConfig);
	}
}
