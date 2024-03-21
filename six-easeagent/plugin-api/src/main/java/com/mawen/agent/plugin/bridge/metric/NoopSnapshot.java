package com.mawen.agent.plugin.bridge.metric;

import java.io.OutputStream;

import com.mawen.agent.plugin.api.metric.Snapshot;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoopSnapshot implements Snapshot {

	INSTANCE;

	private static final long[] EMPTY_LONG_ARRAY = new long[0];

	@Override
	public double getValue(double quantile) {
		return 0D;
	}

	@Override
	public long[] getValues() {
		return EMPTY_LONG_ARRAY;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public long getMax() {
		return 0L;
	}

	@Override
	public double getMean() {
		return 0D;
	}

	@Override
	public long getMin() {
		return 0L;
	}

	@Override
	public double getStdDev() {
		return 0D;
	}

	@Override
	public void dump(OutputStream output) {
		// NOP
	}

	@Override
	public Object unwrap() {
		return null;
	}
}