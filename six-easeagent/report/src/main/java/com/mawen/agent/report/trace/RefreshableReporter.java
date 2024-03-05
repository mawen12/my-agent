package com.mawen.agent.report.trace;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.async.trace.SDKAsyncReporter;
import com.mawen.agent.report.async.trace.TraceAsyncProps;
import com.mawen.agent.report.plugin.ReporterRegistry;
import com.mawen.agent.report.sender.SenderWithEncoder;
import zipkin2.reporter.Reporter;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class RefreshableReporter<S> implements Reporter<S> {

	private SDKAsyncReporter<S> asyncReporter;
	private AsyncProps traceProperties;
	private Config reportConfig;

	public RefreshableReporter(SDKAsyncReporter<S> asyncReporter, Config reportConfig) {
		this.asyncReporter = asyncReporter;
		this.traceProperties = new TraceAsyncProps(reportConfig);
		this.reportConfig = reportConfig;
	}

	@Override
	public void report(S s) {
		this.asyncReporter.report(s);
	}

	public synchronized void refresh(Map<String, String> cfg) {
		String name = cfg.get(TRACE_SENDER_NAME);
		SenderWithEncoder sender = asyncReporter.getSender();
		if (sender != null) {
			if (StringUtils.isNotEmpty(name) && !sender.name().equals(name)) {
				try {
					sender.close();
				}
				catch (IOException e) {
					// ignored
				}
				sender = ReporterRegistry.getSender(TRACE_SENDER, this.reportConfig);
				asyncReporter.setSender(sender);
			}
		} else {
			sender = ReporterRegistry.getSender(TRACE_SENDER, this.reportConfig);
			asyncReporter.setSender(sender);
		}

		traceProperties = new TraceAsyncProps(this.reportConfig);
		asyncReporter.setTraceProperties(traceProperties);
		asyncReporter.setPending(traceProperties.getQueuedMaxItems(), traceProperties.getMessageMaxBytes());
		asyncReporter.setMessageTimeoutNanos(messageTimeout(traceProperties.getMessageMaxBytes()));
		asyncReporter.startFlushThread();
	}

	protected long messageTimeout(long timeout) {
		if (timeout < 0) {
			timeout = 1000L;
		}
		return TimeUnit.MILLISECONDS.toNanos(timeout);
	}
}
