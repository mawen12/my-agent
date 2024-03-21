package com.mawen.agent.report.async.zipkin;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentByteBoundedQueue<S> implements WithSizeConsumer<S> {

	private final LinkedTransferQueue<DataWrapper<S>> queue = new LinkedTransferQueue<>();
	private final AtomicInteger sizeInBytes = new AtomicInteger(0);
	private final int maxSize;
	private final int maxBytes;
	private final LongAdder loseCounter = new LongAdder();

	public AgentByteBoundedQueue(int maxSize, int maxBytes) {
		this.maxSize = maxSize;
		this.maxBytes = maxBytes;
	}

	@Override
	public boolean offer(S next, int nextSizeInBytes) {
		if (maxSize == queue.size()) {
			loseCounter.increment();
			return false;
		}

		if (sizeInBytes.updateAndGet(pre -> pre + nextSizeInBytes) > maxBytes) {
			loseCounter.increment();
			sizeInBytes.updateAndGet(pre -> pre + nextSizeInBytes);
			return false;
		}

		queue.offer(new DataWrapper<>(next, nextSizeInBytes));
		return true;
	}

	public int getCount() {
		return queue.size();
	}

	public int getSizeInBytes() {
		return sizeInBytes.get();
	}

	public int clear() {
		DataWrapper<S> data;
		var result = 0;
		var removeBytes = 0;
		while ((data = queue.poll()) != null) {
			removeBytes += data.getSizeInBytes();
			result++;
		}
		sizeInBytes.addAndGet(removeBytes * -1);
		return result;
	}

	public long getLoseCount() {
		return loseCounter.longValue();
	}

	public int drainTo(WithSizeConsumer<S> consumer, long nanosTimeout) {
		DataWrapper<S> firstPoll;
		try {
			firstPoll = queue.poll(nanosTimeout, TimeUnit.NANOSECONDS);
		}
		catch (InterruptedException e) {
			return 0;
		}
		if (firstPoll == null) {
			return 0;
		}
		return doDrain(consumer, firstPoll);
	}

	int doDrain(WithSizeConsumer<S> consumer, DataWrapper<S> firstPoll) {
		var drainedCount=  0;
		var drainedSizeInBytes = 0;
		var next = firstPoll;
		do {
			int nextSizeInBytes = next.getSizeInBytes();
			if (consumer.offer(next.getElement(), nextSizeInBytes)) {
				drainedCount++;
				drainedSizeInBytes += nextSizeInBytes;
			} else {
				queue.offer(next);
				break;
			}
		} while ((next = queue.poll()) != null);
		final int updateValue = drainedSizeInBytes;
		sizeInBytes.updateAndGet(pre -> pre - updateValue);
		return drainedCount;
	}

	private static class DataWrapper<S> {

		private final S element;
		private final int sizeInBytes;

		public DataWrapper(S element, int sizeInBytes) {
			this.element = element;
			this.sizeInBytes = sizeInBytes;
		}

		public S getElement() {
			return element;
		}

		public int getSizeInBytes() {
			return sizeInBytes;
		}
	}
}
