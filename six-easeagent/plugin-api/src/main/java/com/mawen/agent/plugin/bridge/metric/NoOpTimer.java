package com.mawen.agent.plugin.bridge.metric;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.metric.Snapshot;
import com.mawen.agent.plugin.api.metric.Timer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpTimer implements Timer {

	INSTANCE;

	private static final Timer.Context CONTEXT = new NoOpTimer.Context();

	@Override
	public void update(long duration, TimeUnit unit) {
		// NOP
	}

	@Override
	public void update(Duration duration) {
		// NOP
	}

	@Override
	public <T> T time(Callable<T> event) throws Exception {
		return event.call();
	}

	@Override
	public <T> T timeSupplier(Supplier<T> event) {
		return event.get();
	}

	@Override
	public void time(Runnable event) {
		// NOP
	}

	@Override
	public Timer.Context time() {
		return CONTEXT;
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
	public Snapshot getSnapshot() {
		return NoopSnapshot.INSTANCE;
	}

	@Override
	public Object unwrap() {
		return null;
	}

	private static class Context implements Timer.Context {
		@Override
		public long stop() {
			return 0L;
		}

		@Override
		public void close() {
			// NOP
		}
	}
}
