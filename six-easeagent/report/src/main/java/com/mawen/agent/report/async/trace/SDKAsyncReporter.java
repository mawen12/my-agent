package com.mawen.agent.report.async.trace;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.async.zipkin.AgentBufferNextMessage;
import com.mawen.agent.report.async.zipkin.AgentByteBoundedQueue;
import com.mawen.agent.report.encoder.PackedMessage;
import com.mawen.agent.report.encoder.span.GlobalExtrasSupplier;
import com.mawen.agent.report.sender.SenderWithEncoder;
import com.mawen.agent.report.util.SpanUtils;
import zipkin2.Call;
import zipkin2.CheckResult;
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

	SDKAsyncReporter(Builder builder, Encoder<S> encoder, AsyncProps traceProperties) {
		this.pending = new AgentByteBoundedQueue<>(builder.queuedMaxItems, builder.queuedMaxBytes);
		this.sender = builder.sender;
		this.messageMaxBytes = builder.messageMaxBytes;
		this.messageTimeoutNanos = builder.messageTimeoutNanos;
		this.closeTimeoutNanos = builder.closeTimeoutNanos;
		this.close = new CountDownLatch(builder.messageTimeoutNanos > 0 ? 1 : 0);
		this.metrics = builder.metrics;
		this.encoder = encoder;
		this.traceProperties = traceProperties;
	}

	public static SDKAsyncReporter<ReportSpan> builderSDKAsyncReporter(SenderWithEncoder sender,
			AsyncProps traceProperties, GlobalExtrasSupplier extrasSupplier) {
		var reporter = new Builder(sender, traceProperties)
				.globalExtrasSupplier(extrasSupplier)
				.<ReportSpan>build();

		reporter.setTraceProperties(traceProperties);
		return reporter;
	}

	@Override
	public void flush() {
		if (!this.sender.isAvailable()) {
			return;
		}

		flush(AgentBufferNextMessage.create(encoder, messageMaxBytes, 0), pending);
	}

	@Override
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		try {
			if (!close.await(closeTimeoutNanos, TimeUnit.NANOSECONDS)) {
				logger.warning("Timed out waiting for in-flight spans to send. ");
			}
		}
		catch (InterruptedException e) {
			logger.warning("Interrupted waiting for in-flight spans to send");
			Thread.currentThread().interrupt();
		}
		int count = pending.clear();
		if (count > 0) {
			metrics.incrementSpansDropped(count);
			logger.log(Level.WARNING, "Dropped {0} spans due to AsyncReporter.close()", count);
		}
	}

	@Override
	public void report(S next) {
		if (!this.sender.isAvailable()) {
			return;
		}
		if (!SpanUtils.isValidSpan(next)) {
			return;
		}

		metrics.incrementSpans(1);
		int nextSizeInBytes = encoder.sizeInBytes(next);
		int messageSizeOfNextSpan = encoder.packageSizeInBytes(Collections.singletonList(nextSizeInBytes));
		metrics.incrementSpanBytes(nextSizeInBytes);
		if (closed.get() || messageSizeOfNextSpan > messageMaxBytes || !pending.offer(next, nextSizeInBytes)) {
			metrics.incrementSpansDropped(1);
		}
	}

	@Override
	public CheckResult check() {
		if (sender.isAvailable()) {
			return CheckResult.OK;
		} else {
			return CheckResult.failed(new IOException("Sender is unavailable"));
		}
	}

	public void setSender(SenderWithEncoder sender) {
		this.sender = sender;
		this.encoder = sender.getEncoder();
	}

	public void setPending(int queuedMaxSpans, int queuedMaxBytes) {
		var copyPending = this.pending;
		this.pending = new AgentByteBoundedQueue<>(queuedMaxSpans, queuedMaxBytes);
		consumerData(copyPending);
	}

	public void startFlushThread() {
		if (this.messageTimeoutNanos > 0) {
			var threads = new CopyOnWriteArrayList<Thread>();
			for (int i = 0; i < traceProperties.getReportThread(); i++) {
				AgentBufferNextMessage<S> consumer = AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);
				Thread thread = this.threadFactory.newThread(new Flusher<>(this, consumer, this.sender));
				thread.setName(NAME_PREFIX + "{" + this.sender + "}");
				thread.setDaemon(true);
				thread.start();
				threads.add(thread);
			}
			this.setFlushThreads(threads);
		}
	}

	void flush(AgentBufferNextMessage<S> bundler, AgentByteBoundedQueue<S> pending) {
		if (closed.get()) {
			throw new IllegalStateException("closed");
		}

		pending.drainTo(bundler, bundler.remainingNanos());

		metrics.updateQueuedSpans(pending.getCount());
		metrics.updateQueuedBytes(pending.getSizeInBytes());

		if (!bundler.isReady() && !closed.get()) {
			return;
		}

		metrics.incrementMessages();
		metrics.incrementMessageBytes(bundler.sizeInBytes());

		var message = new PackedMessage.DefaultPackedMessage(bundler.count(), encoder);
		bundler.drain((next, nextSizeInBytes) -> {
			if (message.calculateAppendSize(nextSizeInBytes) < messageMaxBytes) {
				message.addMessage(encoder.encode(next));
				return true;
			} else {
				return false;
			}
		});

		List<EncodedData> nextMessage = message.getMessages();

		try {
			sender.send(nextMessage).execute();
		}
		catch (IOException | RuntimeException e) {
			var count = nextMessage.size();
			Call.propagateIfFatal(e);
			metrics.incrementMessagesDropped(e);
			metrics.incrementSpansDropped(count);

			var logLevel = Level.FINE;
			if (shouldWarnException) {
				logger.log(Level.WARNING, """
      Spans were dropped due to exceptions. All subsequent errors will be logged at FINE level.
						""");
				logLevel = Level.WARNING;
				shouldWarnException = false;
			}

			if (logger.isLoggable(logLevel)) {
				logger.log(logLevel, String.format("Dropped %s spans due to %s(%s)",
						count, e.getClass().getSimpleName(), e.getMessage() == null ? "" : e.getMessage()), e);
			}

			if (e instanceof IllegalStateException t) {
				throw t;
			}
		}
	}

	private void consumerData(AgentByteBoundedQueue<S> copyPending) {
		var thread = this.threadFactory.newThread(() -> {
			AgentBufferNextMessage<S> bufferNextMessage = AgentBufferNextMessage.create(encoder, messageMaxBytes, 0);
			while (copyPending.getCount() > 0) {
				flush(bufferNextMessage, copyPending);
			}
		});
		thread.setName("TempAsyncReporter{" + this.sender + "}");
		thread.setDaemon(true);
		thread.start();
	}

	public AtomicBoolean getClosed() {
		return closed;
	}

	public SenderWithEncoder getSender() {
		return sender;
	}

	public Encoder<S> getEncoder() {
		return encoder;
	}

	public AgentByteBoundedQueue<S> getPending() {
		return pending;
	}

	public int getMessageMaxBytes() {
		return messageMaxBytes;
	}

	public long getMessageTimeoutNanos() {
		return messageTimeoutNanos;
	}

	public long getCloseTimeoutNanos() {
		return closeTimeoutNanos;
	}

	public CountDownLatch getClose() {
		return close;
	}

	public ReporterMetrics getMetrics() {
		return metrics;
	}

	public AsyncProps getTraceProperties() {
		return traceProperties;
	}

	public ThreadFactory getThreadFactory() {
		return threadFactory;
	}

	public boolean isShouldWarnException() {
		return shouldWarnException;
	}

	public List<Thread> getFlushThreads() {
		return flushThreads;
	}

	public void setEncoder(Encoder<S> encoder) {
		this.encoder = encoder;
	}

	public void setPending(AgentByteBoundedQueue<S> pending) {
		this.pending = pending;
	}

	public void setMessageTimeoutNanos(long messageTimeoutNanos) {
		this.messageTimeoutNanos = messageTimeoutNanos;
	}

	public void setTraceProperties(AsyncProps traceProperties) {
		this.traceProperties = traceProperties;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public void setShouldWarnException(boolean shouldWarnException) {
		this.shouldWarnException = shouldWarnException;
	}

	public void setFlushThreads(List<Thread> flushThreads) {
		this.flushThreads = flushThreads;
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

		Builder(SenderWithEncoder sender, AsyncProps traceProperties) {
			if (sender == null) {
				throw new NullPointerException("sender is null");
			}
			this.props = traceProperties;
			this.sender = sender;
			this.messageMaxBytes = traceProperties.getMessageMaxBytes();
			this.queuedMaxItems = traceProperties.getQueuedMaxItems();
			this.messageTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(traceProperties.getMessageTimeout());
			this.queuedMaxBytes = traceProperties.getQueuedMaxSize();
		}

		public Builder threadFactory(ThreadFactory threadFactory) {
			if (threadFactory == null) throw new NullPointerException("threadFactory is null");
			this.threadFactory = threadFactory;
			return this;
		}

		public Builder globalExtrasSupplier(GlobalExtrasSupplier globalExtrasSupplier) {
			this.globalExtrasSupplier = globalExtrasSupplier;
			return this;
		}

		public Builder metrics(ReporterMetrics metrics) {
			if (metrics == null) throw new NullPointerException("metrics is null");
			this.metrics = metrics;
			return this;
		}

		public Builder messageMaxBytes(int messageMaxBytes) {
			if (messageMaxBytes < 0) throw new IllegalArgumentException("messageMaxBytes < 0: " + messageMaxBytes);
			this.messageMaxBytes = messageMaxBytes;
			return this;
		}

		public Builder messageTimeout(long timeout, TimeUnit unit) {
			if (timeout < 0) throw new IllegalArgumentException("messageTimeout < 0: " + timeout);
			if (unit == null) throw new NullPointerException("unit is null");
			this.messageTimeoutNanos = unit.toNanos(timeout);
			return this;
		}

		public Builder closeTimeout(long timeout, TimeUnit unit) {
			if (timeout < 0) throw new IllegalArgumentException("closeTimeout < 0: " + timeout);
			if (unit == null) throw new NullPointerException("unit is null");
			this.closeTimeoutNanos = unit.toNanos(timeout);
			return this;
		}

		public Builder queuedMaxItems(int queuedMaxItems) {
			this.queuedMaxItems = queuedMaxItems;
			return this;
		}

		public Builder queuedMaxBytes(int queuedMaxBytes) {
			this.queuedMaxBytes = queuedMaxBytes;
			return this;
		}

		private <S> SDKAsyncReporter<S> build() {
			Encoder<S> encoder = this.sender.getEncoder();
			if (encoder == null) throw new NullPointerException("encoder is null");

			var result = new SDKAsyncReporter<>(this, encoder, this.props);

			if (this.messageTimeoutNanos > 0) {
				List<Thread> flushThreads = new CopyOnWriteArrayList<>();
				for (int i = 0; i < this.props.getReportThread(); i++) {
					var consumer = AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);

					var flushThread = this.threadFactory
							.newThread(new Flusher<S>(result, consumer, this.sender));
					flushThread.setName(NAME_PREFIX + "{" + this.sender + "}");
					flushThread.setDaemon(true);
					flushThread.start();
					flushThreads.add(flushThread);
				}
				result.setFlushThreads(flushThreads);
				result.setThreadFactory(this.threadFactory);
				result.setSender(this.sender);
			}
			return result;
		}
	}

	public record Flusher<S>(
			SDKAsyncReporter<S> result,
			AgentBufferNextMessage<S> consumer,
			SenderWithEncoder sender) implements Runnable {

		static final Logger logger = Logger.getLogger(Flusher.class.getName());


		@Override
		public void run() {
			try {
				while (!result.closed.get() && sender.isAvailable()) {
					result.flush(consumer, result.pending);
				}
			}
			finally {
				var count = consumer.count();
				if (count > 0) {
					result.metrics.incrementSpansDropped(count);
					logger.log(Level.WARNING, "Dropped {0} spans due to AsyncReporter.close()", count);
				}
				result.close.countDown();
			}
		}

		@Override
		public String toString() {
			return NAME_PREFIX + "{" + result.sender + "}";
		}
	}
}
