package com.mawen.agent.report.async.trace;

import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.report.async.AsyncProps;

import static com.mawen.agent.config.ConfigUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class TraceAsyncProps implements AsyncProps {

	private volatile int reportThread;
	private volatile int queuedMaxSpans;
	private volatile int queuedMaxSize;
	private volatile int messageTimeout;
	private volatile int messageMaxBytes;

	public TraceAsyncProps(Config config) {
		int onePercentageMemory = AsyncProps.onePercentOfMemory();
		bindProp(ReportConfigConst.TRACE_ASYNC_REPORT_THREAD_V2, config, Config::getInt, v -> this.reportThread = v, 1);
		bindProp(ReportConfigConst.TRACE_ASYNC_QUEUED_MAX_SPANS_V2, config, Config::getInt, v -> this.queuedMaxSpans = v, 1000);
		bindProp(ReportConfigConst.TRACE_ASYNC_QUEUED_MAX_SIZE_V2, config, Config::getInt, v -> this.queuedMaxSize = v, onePercentageMemory);
		bindProp(ReportConfigConst.TRACE_ASYNC_MESSAGE_TIMEOUT_V2, config, Config::getInt, v -> this.messageTimeout = v, 1000);
		bindProp(ReportConfigConst.TRACE_ASYNC_MESSAGE_MAX_BYTES_V2, config, Config::getInt, v -> this.messageMaxBytes = v, 999900);
	}

	@Override
	public int getReportThread() {
		return this.reportThread;
	}

	@Override
	public int getQueuedMaxItems() {
		return this.queuedMaxSpans;
	}

	@Override
	public long getMessageTimeout() {
		return this.messageTimeout;
	}

	@Override
	public int getQueuedMaxSize() {
		return this.queuedMaxSize;
	}

	@Override
	public int getMessageMaxBytes() {
		return this.messageMaxBytes;
	}
}
