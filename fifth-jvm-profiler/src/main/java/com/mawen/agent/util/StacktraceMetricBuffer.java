package com.mawen.agent.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class StacktraceMetricBuffer {
	private AtomicLong lastResetMillis = new AtomicLong(System.currentTimeMillis());

	private volatile ConcurrentHashMap<Stacktrace, AtomicLong> metrics = new ConcurrentHashMap<>();

	public void appendValue(Stacktrace stacktrace) {
		AtomicLong counter = metrics.computeIfAbsent(stacktrace, key -> new AtomicLong(0L));
		counter.incrementAndGet();
	}

	public long getLastResetMillis() {
		return lastResetMillis.get();
	}

	public Map<Stacktrace, AtomicLong> reset() {
		ConcurrentHashMap<Stacktrace, AtomicLong> oldCopy = metrics;
		metrics = new ConcurrentHashMap<>();

		lastResetMillis.set(System.currentTimeMillis());

		return oldCopy;
	}
}
