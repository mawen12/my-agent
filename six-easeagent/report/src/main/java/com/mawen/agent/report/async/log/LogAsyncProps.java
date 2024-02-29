package com.mawen.agent.report.async.log;

import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.async.AsyncProps;

import static com.mawen.agent.config.ConfigUtils.*;
import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class LogAsyncProps implements AsyncProps {

	private volatile int reportThread;
	private volatile int queuedMaxLogs;
	private volatile int queuedMaxSize;
	private volatile int messageTimeout;
	private volatile int messageMaxBytes;

	public LogAsyncProps(Config config, String prefix) {
		int onePercentageMemory = AsyncProps.onePercentOfMemory();
		String keyPrefix = StringUtils.isEmpty(prefix) ? LOG_ASYNC : prefix;

		bindProp(join(keyPrefix, join(ASYNC_KEY, ASYNC_THREAD_KEY)),
				config, Config::getInt, v -> this.reportThread = v, 1);

		bindProp(join(keyPrefix, join(ASYNC_KEY, ASYNC_QUEUE_MAX_SIZE_KEY)),
				config, Config::getInt, v -> this.queuedMaxSize = v, onePercentageMemory);

		bindProp(join(keyPrefix, join(ASYNC_KEY, ASYNC_QUEUE_MAX_LOGS_KEY)),
				config, Config::getInt, v -> this.queuedMaxLogs = v, 500);

		bindProp(join(keyPrefix, join(ASYNC_KEY, ASYNC_MSG_MAX_BYTES_KEY)),
				config, Config::getInt, v -> this.messageMaxBytes = v, 999900);

		bindProp(join(keyPrefix, join(ASYNC_KEY, ASYNC_MSG_TIMEOUT_KEY)),
				config, Config::getInt, v -> this.messageTimeout = v, 1000);

	}

	@Override
	public int getReportThread() {
		return this.reportThread;
	}

	@Override
	public int getQueuedMaxItems() {
		return this.queuedMaxLogs;
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
