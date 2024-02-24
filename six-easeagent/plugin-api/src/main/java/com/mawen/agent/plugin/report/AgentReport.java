package com.mawen.agent.plugin.report;

import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;

/**
 * report interface: trace/metric/tracing
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface AgentReport {

	/**
	 * report trace span
	 *
	 * @param span trace span
	 */
	void report(ReportSpan span);

	/**
	 * report access-log
	 *
	 * @param log log info
	 */
	void report(AccessLogInfo log);

	/**
	 * report application log
	 *
	 * @param log
	 */
	void report(AgentLogData log);

	/**
	 * Metric reporters factory
	 *
	 * @return metric reporters factory
	 */
	MetricReporterFactory metricReporter();
}
