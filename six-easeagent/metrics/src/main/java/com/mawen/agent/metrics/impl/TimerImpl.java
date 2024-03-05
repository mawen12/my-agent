package com.mawen.agent.metrics.impl;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.metric.Snapshot;
import com.mawen.agent.plugin.api.metric.Timer;
import com.mawen.agent.plugin.bridge.NoOpMetrics;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class TimerImpl implements Timer {

	private final com.codahale.metrics.Timer timer;

	private TimerImpl(com.codahale.metrics.Timer timer) {
		this.timer = Objects.requireNonNull(timer, "timer must not be null");
	}

	public static Timer build(com.codahale.metrics.Timer timer) {
		return timer == null ? NoOpMetrics.NO_OP_TIMER : new TimerImpl(timer);
	}

	@Override
	public void update(long duration, TimeUnit unit) {
		timer.update(duration, unit);
	}

	@Override
	public void update(Duration duration) {
		timer.update(duration);
	}

	@Override
	public <T> T time(Callable<T> event) throws Exception {
		return timer.time(event);
	}

	@Override
	public <T> T timeSupplier(Supplier<T> event) {
		return timer.timeSupplier(event);
	}

	@Override
	public void time(Runnable event) {
		timer.time(event);
	}

	@Override
	public Context time() {
		return new ContextImpl(timer.time());
	}

	@Override
	public long getCount() {
		return timer.getCount();
	}

	@Override
	public double getFifteenMinuteRate() {
		return timer.getFifteenMinuteRate();
	}

	@Override
	public double getFiveMinuteRate() {
		return timer.getFiveMinuteRate();
	}

	@Override
	public double getMeanRate() {
		return timer.getMeanRate();
	}

	@Override
	public double getOneMinuteRate() {
		return timer.getOneMinuteRate();
	}

	@Override
	public Snapshot getSnapshot() {
		return SnapshotImpl.build(timer.getSnapshot());
	}

	@Override
	public Object unwrap() {
		return timer;
	}

	public static class ContextImpl implements Context {

		private final com.codahale.metrics.Timer.Context context;

		public ContextImpl(com.codahale.metrics.Timer.Context context) {
			this.context = context;
		}

		@Override
		public long stop() {
			return context.stop();
		}

		@Override
		public void close() {
			context.close();
		}
	}
}
