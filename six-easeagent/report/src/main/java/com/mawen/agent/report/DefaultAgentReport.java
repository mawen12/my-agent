package com.mawen.agent.report;

import java.util.List;

import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultAgentReport implements AgentReport, ConfigChangeListener {

	private final TraceReport traceReport;
	private final MetricReporterFactory metricReporterFactory;
	private final AccessLogReporter accessLogReporter;
	private final ApplicationLogReporter applicationLogReporter;
	private final Config config;
	private final Config reportConfig;

	public DefaultAgentReport(Config config) {
		this.traceReport = traceReport;
		this.metricReporterFactory = metricReporterFactory;
		this.accessLogReporter = accessLogReporter;
		this.applicationLogReporter = applicationLogReporter;
		this.config = config;
		this.reportConfig = reportConfig;
	}

	@Override
	public void onChange(List<ChangeItem> list) {

	}

	@Override
	public void report(ReportSpan span) {

	}

	@Override
	public void report(AccessLogInfo log) {

	}

	@Override
	public void report(AgentLogData log) {

	}

	@Override
	public MetricReporterFactory metricReporter() {
		return null;
	}
}
