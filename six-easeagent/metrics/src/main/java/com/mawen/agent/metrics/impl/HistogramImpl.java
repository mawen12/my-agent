package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Snapshot;
import com.mawen.agent.plugin.bridge.NoOpMetrics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class HistogramImpl implements Histogram {

	private final com.codahale.metrics.Histogram histogram;

	private HistogramImpl(com.codahale.metrics.Histogram histogram) {
		this.histogram = Objects.requireNonNull(histogram, "histogram must not be null");
	}

	public static Histogram build(com.codahale.metrics.Histogram histogram) {
		return histogram == null ? NoOpMetrics.NO_OP_HISTOGRAM : new HistogramImpl(histogram);
	}

	@Override
	public void update(int value) {
		histogram.update(value);
	}

	@Override
	public void update(long value) {
		histogram.update(value);
	}

	@Override
	public long getCount() {
		return histogram.getCount();
	}

	@Override
	public Snapshot getSnapshot() {
		return SnapshotImpl.build(histogram.getSnapshot());
	}

	@Override
	public Object unwrap() {
		return histogram;
	}
}
