package com.mawen.agent.mock.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.mock.config.MockConfig;
import com.mawen.agent.mock.report.impl.LastJsonReporter;
import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.metric.MetricReporterFactory;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.plugin.utils.common.JsonUtil;
import com.mawen.agent.report.DefaultAgentReport;
import com.mawen.agent.report.util.SpanUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public class MockReport {

	private static final Logger log = LoggerFactory.getLogger(MockReport.class);

	private static final AgentReport AGENT_REPORT = new MockAgentReport(DefaultAgentReport.create(MockConfig.getConfigs()));

	private static final AtomicReference<AccessLogInfo> LAST_ACCESS_LOG = new AtomicReference<>();
	private static final AtomicReference<ReportSpan> LAST_SPAN = new AtomicReference<>();
	private static final AtomicReference<ReportSpan> LAST_SKIP_SPAN = new AtomicReference<>();

	private static volatile MetricFlushable metricFlushable;
	private static volatile MockSpanReport mockSpanReport;
	private static volatile Reporter metricReportMock;
	private static volatile JsonReporter metricJsonReport;

	public static AgentReport getAgentReport() {
		return AGENT_REPORT;
	}

	public static void setMockSpanReport(MockSpanReport mockSpanReport) {
		MockReport.mockSpanReport = mockSpanReport;
	}

	public static void setMockMetricReport(Reporter mockMetricReport) {
		MockReport.metricReportMock = mockMetricReport;
	}

	public static LastJsonReporter lastMetricJsonReporter(Predicate<Map<String, Object>> filter) {
		LastJsonReporter lastJsonReporter = new LastJsonReporter(filter, metricFlushable);
		metricJsonReport = lastJsonReporter;
		return lastJsonReporter;
	}

	public static void cleanReporter() {
		mockSpanReport = null;
		metricReportMock = null;
		metricJsonReport = null;
	}

	private static void reportMetricToJson(String text) {
		if (metricReportMock != null) {
			List<Map<String, Object>> json;
			if (text.trim().startsWith("{")) {
				Map<String, Object> jsonMap = JsonUtil.toMap(text);
				json = Collections.singletonList(jsonMap);
			} else {
				json = JsonUtil.toList(text);
			}
			metricJsonReport.report(json);
		}
	}

	public static ReportSpan getLastSpan() {
		return LAST_SPAN.get();
	}

	public static void cleanLastSpan() {
		LAST_SPAN.set(null);
	}

	public static AccessLogInfo getLastAccessLog() {
		return LAST_ACCESS_LOG.get();
	}

	public static void cleanLastAccessLog() {
		LAST_ACCESS_LOG.set(null);
	}

	public static ReportSpan getLastSkipSpan() {
		return LAST_SKIP_SPAN.get();
	}

	public static void cleanLastSkipSpan() {
		LAST_SKIP_SPAN.set(null);
	}

	public static class MockAgentReport implements AgentReport {

		private final AgentReport agentReport;
		private final MockMetricReporterFactory pluginMetricFactory;

		public MockAgentReport(AgentReport agentReport) {
			this.agentReport = agentReport;
			this.pluginMetricFactory = new MockMetricReporterFactory(agentReport.metricReporter());
		}

		@Override
		public void report(ReportSpan span) {
			agentReport.report(span);
			if (!SpanUtils.isValidSpan(span)) {
				log.warn("span<traceId({}), id({}), name({}), kind({})> not start(), skip it.", span.traceId(), span.id(), span.name(), span.kind());
				LAST_SKIP_SPAN.set(span);
				return;
			}
			if (span.duration() == 0) {
				log.warn(String.format("span<traceId(%s), id(%s), name(%s), kind(%s), timestamp(%s) duration(%s) not finish, skip it.", span.traceId(), span.id(), span.name(), span.kind(), span.timestamp(), span.duration()));
				LAST_SKIP_SPAN.set(span);
				return;
			}
			LAST_SPAN.set(span);
			MockSpanReport mockSpanReport = MockReport.mockSpanReport;
			if (mockSpanReport != null) {
				mockSpanReport.report(span);
			}
		}

		@Override
		public void report(AccessLogInfo log) {
			LAST_ACCESS_LOG.set(log);
		}

		@Override
		public MetricReporterFactory metricReporter() {
			return pluginMetricFactory;
		}
	}

	public record MockMetricReporterFactory(MetricReporterFactory metricReporterFactory) implements MetricReporterFactory {

		@Override
		public Reporter reporter(IPluginConfig config) {
			return new MockReporter(metricReporterFactory.reporter(config));
		}
	}

	public record MockReporter(Reporter delegate) implements Reporter {

		@Override
		public void report(String msg) {
			delegate.report(msg);
			Reporter reportMock = metricReportMock;
			if (reportMock != null) {
				reportMock.report(msg);
			}
			reportMetricToJson(msg);
		}

		@Override
		public void report(EncodedData msg) {
			report(new String(msg.getData()));
		}
	}
}
