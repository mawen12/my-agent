package com.mawen.agent.metrics.impl;

import java.util.Objects;

import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.bridge.NoOpMetrics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MeterImpl implements Meter {

	private final com.codahale.metrics.Meter meter;

	private MeterImpl(com.codahale.metrics.Meter meter) {
		this.meter = Objects.requireNonNull(meter, "meter must not be null");
	}

	public static Meter build(com.codahale.metrics.Meter meter) {
		return meter == null ? NoOpMetrics.NO_OP_METER : new MeterImpl(meter);
	}

	@Override
	public void mark() {
		meter.mark();
	}

	@Override
	public void mark(long n) {
		meter.mark(n);
	}

	@Override
	public long getCount() {
		return meter.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return meter.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return meter.getFiveMinuteRate();
	}

	@Override
	public double getMeanRate() {
		return meter.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return meter.getOneMinuteRate();
	}

	@Override
	public Object unwrap() {
		return meter;
	}
}
