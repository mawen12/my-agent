package com.mawen.agent.report.async.zipkin;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentBufferNextMessage<S> implements WithSizeConsumer<S> {

	final Encoder<S> encoder;
	final int maxBytes;
	final long timeoutNanos;
	final List<S> spans = new ArrayList<>();
	final List<Integer> sizes = new ArrayList<>();

	long deadlineNanoTime;
	int packageSizeInBytes;
	boolean bufferFull;

	AgentBufferNextMessage(Encoder<S> encoder, int maxBytes, long timeoutNanos) {
		this.encoder = encoder;
		this.maxBytes = maxBytes;
		this.timeoutNanos = timeoutNanos;
		resetMessageSizeInBytes();
	}

	public static <S> AgentBufferNextMessage<S> create(Encoder<S> encoder, int maxBytes, long timeoutNanos) {
		return new AgentBufferNextMessage<>(encoder, maxBytes, timeoutNanos);
	}

	public boolean offer(S next, int nextSizeInBytes) {
		int x = messageSizeInBytes(nextSizeInBytes);
		int includingNextVsMaxBytes = Integer.compare(x, maxBytes);

		if (includingNextVsMaxBytes > 0) {
			bufferFull = true;
			return false;
		}

		addSpanToBuffer(next, nextSizeInBytes);
		packageSizeInBytes = x;

		if (includingNextVsMaxBytes == 0) {
			bufferFull = true;
		}
		return true;
	}

	public long remainingNanos() {
		if (spans.isEmpty()) {
			deadlineNanoTime = System.nanoTime() + timeoutNanos;
		}
		return Math.max(deadlineNanoTime - System.nanoTime(), 0);
	}

	public boolean isReady() {
		return bufferFull || remainingNanos() <= 0;
	}

	public void drain(WithSizeConsumer<S> consumer) {
		Iterator<S> spanIterator = spans.iterator();
		Iterator<Integer> sizeIterator = sizes.iterator();
		while (spanIterator.hasNext()) {
			if (consumer.offer(spanIterator.next(), sizeIterator.next())) {
				bufferFull = false;
				spanIterator.remove();
				sizeIterator.remove();
			} else {
				break;
			}
		}

		resetMessageSizeInBytes();
		deadlineNanoTime = 0;
	}

	public int count() {
		return spans.size();
	}

	public int sizeInBytes() {
		return packageSizeInBytes;
	}

	int messageSizeInBytes(int nextSizeInBytes) {
		return packageSizeInBytes + encoder.appendSizeInBytes(nextSizeInBytes);
	}

	void resetMessageSizeInBytes() {
		packageSizeInBytes = encoder.packageSizeInBytes(sizes);
	}

	void addSpanToBuffer(S next, int nextSizeInBytes) {
		spans.add(next);
		sizes.add(nextSizeInBytes);
	}
}
