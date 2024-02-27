package com.mawen.agent.report.async.trace;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.sender.SenderWithEncoder;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.ReporterMetrics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class SDKAsyncReporter<S> extends AsyncReporter<S> {
	private static final Logger logger = Logger.getLogger(SDKAsyncReporter.class.getName());

	private static final String NAME_PREFIX = "AsyncReporter";

	final AtomicBoolean closed = new AtomicBoolean(false);
	SenderWithEncoder sender;
	Encoder<S> encoder;
	AgentByteBoundedQueue<S> pending;
	final int messageMaxBytes;
	long messageTimeoutNanos;
	final long closeTimeoutNanos;
	final CountDownLatch close;
	final ReporterMetrics metrics;
	AsyncProps traceProperties;

	ThreadFactory threadFactory;

	private boolean shouldWarnException = true;

	List<Thread> flushThreads;

	SDKAsyncReporter() {

	}

	public static final class Builder {
		final SenderWithEncoder sender;
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		ReporterMetrics metrics = ReporterMetrics.NOOP_METRICS;
		int messageMaxBytes;
		long messageTimeoutNanos;
		long closeTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
		int queuedMaxItems;
		int queuedMaxBytes;
		AsyncProps props;
		GlobalExtrasSupplier globalExtrasSupplier;


	}
}
