package com.mawen.agent.report;

import java.util.List;

import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigAdapter;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.async.log.AccessLogReporter;
import com.mawen.agent.report.async.log.ApplicationLogReporter;
import com.mawen.agent.report.trace.TraceReport;

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
		this.config = config;
		this.reportConfig = new Configs(ReportConfigAdapter.extractReporterConfig(config));
		this.traceReport = new TraceReport(this.reportConfig);
		this.metricReporterFactory = metricReporterFactory;
		this.accessLogReporter = accessLogReporter;
		this.applicationLogReporter = applicationLogReporter;
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
