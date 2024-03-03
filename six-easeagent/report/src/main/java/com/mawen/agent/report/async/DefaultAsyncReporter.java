package com.mawen.agent.report.async;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.report.async.zipkin.AgentByteBoundedQueue;
import com.mawen.agent.report.sender.SenderWithEncoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class DefaultAsyncReporter<S> implements AsyncReporter<S> {

	static final Logger logger = Logger.getLogger(DefaultAsyncReporter.class.getName());
	static final String NAME_PREFIX = "DefaultAsyncReporter";
	final AsyncReporterMetrics metrics;

	final AtomicBoolean closed = new AtomicBoolean(false);

	AgentByteBoundedQueue<S> pending;
	final CountDownLatch close;

	final int messageMaxBytes;
	final long closeTimeoutNanos;
	long messageTimeoutNanos;
	ThreadFactory threadFactory;

	SenderWithEncoder sender;
	Encoder<S> encoder;

	AsyncProps asyncProperties;

	private boolean shouldWarnException = true;

	List<Thread> flushThreads;

	DefaultAsyncReporter(Builder builder, AsyncProps asyncProperties) {

	}

	@Override
	public void setFlushThreads(List<Thread> flushThreads) {

	}

	@Override
	public void setSender(SenderWithEncoder sender) {

	}

	@Override
	public SenderWithEncoder getSender() {
		return null;
	}

	@Override
	public void setPending(int queuedMaxSpans, int queuedMaxBytes) {

	}

	@Override
	public void setMessageTimeoutNanos(long messageTimeoutNanos) {

	}

	@Override
	public void report(S next) {

	}

	@Override
	public void flush() {

	}

	@Override
	public boolean check() {
		return false;
	}

	@Override
	public void close() {

	}

	@Override
	public void setThreadFactory(ThreadFactory threadFactory) {

	}

	@Override
	public void startFlushThread() {

	}

	@Override
	public void closeFlushThread() {

	}

	@Override
	public void onChange(List<ChangeItem> list) {

	}

	public static final class Builder {
		final SenderWithEncoder encoder;
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		AsyncReporterMetrics metrics = AsyncReporterMetrics.NOOP_METRICS;
		int messageMaxBytes;
		long messageTimeoutNanos;
		long closeTimeoutNanos = TimeUnit.SECONDS.toNanos(1);


	}
}
