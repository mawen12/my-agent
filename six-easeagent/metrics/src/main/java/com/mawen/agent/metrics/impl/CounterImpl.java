package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.bridge.metric.NoOpCounter;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class CounterImpl implements Counter {

	private final com.codahale.metrics.Counter counter;

	private CounterImpl(com.codahale.metrics.Counter counter) {
		this.counter = Objects.requireNonNull(counter, "counter must not be null");
	}

	public static Counter build(com.codahale.metrics.Counter counter) {
		return NoNull.of(new CounterImpl(counter), NoOpCounter.INSTANCE);
	}

	@Override
	public void inc() {
		counter.inc();
	}

	@Override
	public void inc(long n) {
		counter.inc(n);
	}

	@Override
	public void dec() {
		counter.dec();
	}

	@Override
	public void dec(long n) {
		counter.dec(n);
	}

	@Override
	public long getCount() {
		return counter.getCount();
	}

	@Override
	public Object unwrap() {
		return counter;
	}
}
