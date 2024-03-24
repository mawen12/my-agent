package com.mawen.agent.report;

import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigAdapter;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.async.log.AccessLogReporter;
import com.mawen.agent.report.metric.MetricReporterFactoryImpl;
import com.mawen.agent.report.plugin.ReporterLoader;
import com.mawen.agent.report.trace.TraceReport;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class DefaultAgentReport implements AgentReport {

	private final TraceReport traceReport;
	private final MetricReporterFactory metricReporterFactory;
	private final AccessLogReporter accessLogReporter;
	private final Config reportConfig;

	public DefaultAgentReport(Config config) {
		this.reportConfig = new Configs(ReportConfigAdapter.extractReporterConfig(config));
		this.traceReport = new TraceReport(this.reportConfig);
		this.accessLogReporter = new AccessLogReporter(reportConfig);
		this.metricReporterFactory = MetricReporterFactoryImpl.create(reportConfig);
	}

	public static AgentReport create(Config config) {
		ReporterLoader.load();
		return new DefaultAgentReport(config);
	}
	@Override
	public void report(ReportSpan log) {
		this.traceReport.report(log);
	}

	@Override
	public void report(AccessLogInfo log) {
		this.accessLogReporter.report(log);
	}

	@Override
	public MetricReporterFactory metricReporter() {
		return this.metricReporterFactory;
	}
}
