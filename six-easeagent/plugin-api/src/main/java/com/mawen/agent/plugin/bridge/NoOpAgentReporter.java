package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum NoOpAgentReporter implements AgentReport {
	INSTANCE;

	@Override
	public void report(ReportSpan span) {
		// NOP
	}

	@Override
	public void report(AccessLogInfo log) {
		// NOP
	}

	@Override
	public MetricReporterFactory metricReporter() {
		return config -> NoOpReporter.INSTANCE;
	}
}
