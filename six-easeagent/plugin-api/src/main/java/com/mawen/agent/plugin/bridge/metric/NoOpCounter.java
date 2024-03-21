package com.mawen.agent.plugin.bridge.metric;

import com.mawen.agent.plugin.api.metric.Counter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpCounter implements Counter {

	INSTANCE;

	@Override
	public void inc() {
		// NOP
	}

	@Override
	public void inc(long n) {
		// NOP
	}

	@Override
	public void dec() {
		// NOP
	}

	@Override
	public void dec(long n) {
		// NOP
	}

	@Override
	public long getCount() {
		return 0L;
	}

	@Override
	public Object unwrap() {
		return null;
	}
}
