package com.mawen.agent.report.async;

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
import com.mawen.agent.report.async.zipkin.AgentBufferNextMessage;
import com.mawen.agent.report.async.zipkin.AgentByteBoundedQueue;
import com.mawen.agent.report.encoder.PackedMessage;
import com.mawen.agent.report.encoder.span.GlobalExtrasSupplier;
import com.mawen.agent.report.sender.SenderWithEncoder;
import zipkin2.Call;

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
		this.asyncProperties = asyncProperties;

		this.pending = new AgentByteBoundedQueue<>(builder.queuedMaxItems, builder.queuedMaxBytes);
		this.messageMaxBytes = builder.messageMaxBytes;
		this.messageTimeoutNanos = builder.messageTimeoutNanos;
		this.closeTimeoutNanos = builder.closeTimeoutNanos;
		this.close = new CountDownLatch(builder.messageTimeoutNanos > 0 ? 1 : 0);

		this.metrics = builder.metrics;
		this.sender = builder.sender;
		this.encoder = builder.sender.getEncoder();
	}

	public static <S> AsyncReporter<S> create(SenderWithEncoder sender, AsyncProps asyncProperties) {
		return new Builder(sender, asyncProperties).build();
	}

	@Override
	public void setFlushThreads(List<Thread> flushThreads) {
		this.flushThreads = flushThreads;
	}

	@Override
	public void setSender(SenderWithEncoder sender) {
		this.sender = sender;
		this.encoder = sender.getEncoder();
	}

	@Override
	public SenderWithEncoder getSender() {
		return this.sender;
	}

	@Override
	public void setPending(int queuedMaxSpans, int queuedMaxBytes) {
		var copyPending = this.pending;
		this.pending = new AgentByteBoundedQueue<>(queuedMaxSpans, queuedMaxBytes);
		consumerData(copyPending);
	}

	@Override
	public void setMessageTimeoutNanos(long messageTimeoutNanos) {
		this.messageTimeoutNanos = messageTimeoutNanos;
	}

	@Override
	public void report(S next) {
		if (!this.sender.isAvailable()) {
			return;
		}

		metrics.incrementItems(1);
		var nextSizeInBytes = encoder.sizeInBytes(next);
		var messageSizeOfNextSpan = encoder.packageSizeInBytes(Collections.singletonList(nextSizeInBytes));
		metrics.incrementSpanBytes(nextSizeInBytes);
		if (closed.get() || messageSizeOfNextSpan > messageMaxBytes || !pending.offer(next,nextSizeInBytes)) {
			metrics.incrementItemsDropped(1);
		}
	}

	@Override
	public void flush() {
		if (!this.sender.isAvailable()) {
			return;
		}

		flush(AgentBufferNextMessage.create(encoder, messageMaxBytes, 0), pending);
	}

	@Override
	public boolean check() {
		return sender.isAvailable();
	}

	@Override
	public void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		try {
			if (!close.await(closeTimeoutNanos, TimeUnit.NANOSECONDS)) {
				logger.warning("Timed out waiting for in-flight spans to send");
			}
		}
		catch (InterruptedException e) {
			logger.warning("Interrupted waiting for in-flight spans to send");
			Thread.currentThread().interrupt();
		}
		var count = pending.clear();
		if (count > 0) {
			metrics.incrementItemsDropped(count);
			logger.log(Level.WARNING,"Dropped {0} spans due to AsyncReporter.close()", count);
		}
	}

	@Override
	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	@Override
	public void startFlushThread() {
		if (this.messageTimeoutNanos > 0) {
			var flushThreads = new CopyOnWriteArrayList<Thread>();
			for (var i = 0; i < asyncProperties.getReportThread(); i++) {
				final var consumer = AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);
				var flushThread = this.threadFactory.newThread(new Flusher<>(this, consumer));
				flushThread.setName(NAME_PREFIX + "{" + this.sender + "}");
				flushThread.setDaemon(true);
				flushThread.start();
			}
			this.setFlushThreads(flushThreads);
		}
	}

	@Override
	public void closeFlushThread() {
		for (var thread : this.flushThreads) {
			thread.interrupt();
		}
	}

	void flush(AgentBufferNextMessage<S> bundler, AgentByteBoundedQueue<S> pending) {
		if (closed.get()) {
			throw new IllegalArgumentException("closed");
		}

		pending.drainTo(bundler, bundler.remainingNanos());

		// record after flushing reduces the amount of gauge events vs on doing this on report
		metrics.updateQueuedItems(pending.getCount());
		metrics.updateQueuedBytes(pending.getSizeInBytes());

		// loop around if we are running, and the bundle isn't full
		// if we are closed, try to send what's pending
		if (!bundler.isReady() && !closed.get()) {
			return;
		}

		// Signal that we are about to send a message of a known size in bytes
		metrics.incrementMessages();
		metrics.incrementMessageBytes(bundler.sizeInBytes());

		// Create the next message, Since we are outside the lock shared with writers, we can encode
		PackedMessage message = new PackedMessage.DefaultPackedMessage(bundler.count(), encoder);
		bundler.drain(((next, nextSizeInBytes) -> {
			if (message.calculateAppendSize(nextSizeInBytes) <= messageMaxBytes) {
				message.addMessage(encoder.encode(next));
				return true;
			} else {
				return false;
			}
		}));

		List<EncodedData> nextMessage = message.getMessages();
		try {
			sender.send(nextMessage).execute();
		}
		catch (IOException | RuntimeException t) {
			var count = nextMessage.size();
			Call.propagateIfFatal(t);
			metrics.incrementMessagesDropped(t);
			metrics.incrementItemsDropped(count);

			Level logLevel = Level.FINE;

			if (shouldWarnException) {
				logger.log(Level.WARNING, "Spans were dropped due to exceptions. All subsequent errors will be logged at FINE level.");
				logLevel = Level.WARNING;
				shouldWarnException = false;
			}

			if (logger.isLoggable(logLevel)) {
				logger.log(logLevel,String.format("Dropped %s spans due to %s(%s)", count, t.getClass().getSimpleName(), t.getMessage() == null ? "" : t.getMessage()), t);
			}

			// Raise in case the sender was closed out-of-band.
			if (t instanceof IllegalStateException) {
				throw (IllegalStateException)t;
			}
		}
	}

	private void consumerData(final AgentByteBoundedQueue<S> copyPending) {
		var flushThread = this.threadFactory.newThread(() -> {
			final AgentBufferNextMessage<S> bufferNextMessage = AgentBufferNextMessage.create(encoder, this.messageMaxBytes, 0);
			while (copyPending.getCount() > 0) {
				flush(bufferNextMessage,copyPending);
			}
		});
		flushThread.setName("TempAsyncReporter{" + this.sender + "}");
		flushThread.setDaemon(true);
		flushThread.start();
	}

	public static final class Builder {
		final SenderWithEncoder sender;
		ThreadFactory threadFactory = Executors.defaultThreadFactory();
		AsyncReporterMetrics metrics = AsyncReporterMetrics.NOOP_METRICS;
		int messageMaxBytes;
		long messageTimeoutNanos;
		long closeTimeoutNanos = TimeUnit.SECONDS.toNanos(1);
		int queuedMaxItems;
		int queuedMaxBytes;
		AsyncProps props;
		GlobalExtrasSupplier globalExtrasSupplier;

		static int onePercentOfMemory() {
			long result = (long) (Runtime.getRuntime().totalMemory() * 0.01);
			return (int) Math.max(Math.min(Integer.MAX_VALUE, result), Integer.MIN_VALUE);
		}

		Builder (SenderWithEncoder sender, AsyncProps asyncProperties){
			if (sender == null) {
				throw new NullPointerException("sender == null");
			}
			this.props = asyncProperties;
			this.sender = sender;
			this.messageMaxBytes = asyncProperties.getMessageMaxBytes();
			this.queuedMaxItems = asyncProperties.getQueuedMaxItems();
			this.messageTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(asyncProperties.getMessageTimeout());
			this.queuedMaxBytes = asyncProperties.getQueuedMaxSize();
		}

		public Builder threadFactory(ThreadFactory threadFactory) {
			if (threadFactory == null) throw new NullPointerException("threadFactory == null");
			this.threadFactory = threadFactory;
			return this;
		}

		public Builder metrics(AsyncReporterMetrics metrics) {
			if (metrics == null) throw new NullPointerException("metrics == null");
			this.metrics = metrics;
			return this;
		}

		public Builder messageMaxBytes(int messageMaxBytes) {
			if (messageMaxBytes < 0) throw new IllegalArgumentException("messageMaxBytes < 0: " + messageMaxBytes);
			this.messageMaxBytes = Math.min(messageMaxBytes, props.getMessageMaxBytes());
			return this;
		}

		public Builder messageTimeout(long timeout, TimeUnit unit) {
			if (timeout < 0) throw new IllegalArgumentException("messageTimeout < 0: " + timeout);
			if (unit == null) throw new NullPointerException("unit == null");
			this.messageTimeoutNanos = unit.toNanos(timeout);
			return this;
		}

		public Builder closeTimeout(long timeout, TimeUnit unit) {
			if (timeout < 0) throw new IllegalArgumentException("closeTimeout < 0: " + timeout);
			if (unit == null) throw new NullPointerException("unit == null");
			this.closeTimeoutNanos = unit.toNanos(timeout);
			return this;
		}

		public Builder queuedMaxBytes(int queuedMaxBytes) {
			this.queuedMaxBytes = queuedMaxBytes;
			return this;
		}

		public Builder queuedMaxItems(int queuedMaxItems) {
			this.queuedMaxItems = queuedMaxItems;
			return this;
		}

		private <S> DefaultAsyncReporter<S> build() {
			Encoder<S> encoder = this.sender.getEncoder();
			if (encoder == null) throw new NullPointerException("encoder == null");

			final var result = new DefaultAsyncReporter<S>(this, this.props);

			if (this.messageTimeoutNanos > 0) {
				// Start a thread that flushes the queue in a loop
				List<Thread> flushThreads = new CopyOnWriteArrayList<>();
				for (var i = 0; i < this.props.getReportThread(); i++) {
					final var consumer = AgentBufferNextMessage.create(encoder, this.messageMaxBytes, this.messageTimeoutNanos);

					var flushThread = this.threadFactory.newThread(new Flusher<>(result, consumer));
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

	public static final class Flusher<S> implements Runnable {

		static final Logger logger = Logger.getLogger(Flusher.class.getName());

		final DefaultAsyncReporter<S> reporter;
		final AgentBufferNextMessage<S> consumer;

		public Flusher(DefaultAsyncReporter<S> reporter, AgentBufferNextMessage<S> consumer) {
			this.reporter = reporter;
			this.consumer = consumer;
		}

		@Override
		public void run() {
			try {
				while (!reporter.closed.get() && reporter.check()) {
					reporter.flush(consumer, reporter.pending);
				}
			} finally {
				var count = consumer.count();
				if (count > 0) {
					reporter.metrics.incrementItemsDropped(count);
					logger.log(Level.WARNING, "Dropped {0} spans due to AsyncReporter.close()", count);
				}
				reporter.close.countDown();
			}
		}

		@Override
		public String toString() {
			return NAME_PREFIX + "{" + reporter.sender + "}";
		}
	}
}
