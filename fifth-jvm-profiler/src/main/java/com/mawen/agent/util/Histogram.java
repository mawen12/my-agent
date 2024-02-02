package com.mawen.agent.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class Histogram {

	private AtomicLong count = new AtomicLong(0);
	private AtomicLong sum = new AtomicLong(0);
	private AtomicLong min = new AtomicLong(Long.MAX_VALUE);
	private AtomicLong max = new AtomicLong(Long.MIN_VALUE);

	public void appendValue(long value) {
		count.incrementAndGet();
		sum.addAndGet(value);

		min.updateAndGet(x -> Math.min(value, x));
		max.updateAndGet(x -> Math.max(value, x));
	}

	public Long getCount() {
		return count.get();
	}

	public Long getSum() {
		return sum.get();
	}

	public Long getMin() {
		return min.get();
	}

	public Long getMax() {
		return max.get();
	}
}
