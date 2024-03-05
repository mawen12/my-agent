package com.mawen.agent.metrics.impl;

import java.io.OutputStream;
import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Snapshot;
import com.mawen.agent.plugin.bridge.NoOpMetrics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class SnapshotImpl implements Snapshot {

	private final com.codahale.metrics.Snapshot snapshot;

	private SnapshotImpl(com.codahale.metrics.Snapshot snapshot) {
		this.snapshot = Objects.requireNonNull(snapshot, "Snapshot must not be null");
	}

	public static Snapshot build(com.codahale.metrics.Snapshot snapshot) {
		return snapshot == null ? NoOpMetrics.NO_OP_SNAPSHOT : new SnapshotImpl(snapshot);
	}

	@Override
	public double getValue(double quantile) {
		return snapshot.getValue(quantile);
	}

	@Override
	public long[] getValues() {
		return snapshot.getValues();
	}

	@Override
	public int size() {
		return snapshot.size();
	}

	@Override
	public long getMax() {
		return snapshot.getMax();
	}

	@Override
	public double getMean() {
		return snapshot.getMean();
	}

	@Override
	public long getMin() {
		return snapshot.getMin();
	}

	@Override
	public double getStdDev() {
		return snapshot.getStdDev();
	}

	@Override
	public void dump(OutputStream output) {
		snapshot.dump(output);
	}

	@Override
	public Object unwrap() {
		return snapshot;
	}
}
