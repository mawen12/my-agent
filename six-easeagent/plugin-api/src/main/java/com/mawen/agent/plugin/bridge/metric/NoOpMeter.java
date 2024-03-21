package com.mawen.agent.plugin.bridge.metric;

import com.mawen.agent.plugin.api.metric.Meter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpMeter implements Meter {

	INSTANCE;

	@Override
	public void mark() {
		// NOP
	}

	@Override
	public void mark(long n) {
		// NOP
	}

	@Override
	public long getCount() {
		return 0L;
	}

	@Override
	public double getFifteenMinuteRate() {
		return 0D;
	}

	@Override
	public double getFiveMinuteRate() {
		return 0D;
	}

	@Override
	public double getMeanRate() {
		return 0D;
	}

	@Override
	public double getOneMinuteRate() {
		return 0D;
	}

	@Override
	public Object unwrap() {
		return null;
	}
}
