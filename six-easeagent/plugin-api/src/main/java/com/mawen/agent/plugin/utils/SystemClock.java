package com.mawen.agent.plugin.utils;

import java.sql.Timestamp;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Optimization of System.currentTimeMillis() performance problem in high concurrency scenario
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 * @see <a href="https://pzemtsov.github.io/2017/07/23/the-slow-currenttimemillis.html">The slow currentTimeMillis()</a>
 * @see <a href="https://programmer.group/5e85bd0cc8b52.html">Optimization of System.currentTimeMillis() performance in high concurrency sceneario</a>
 */
public class SystemClock {
	private final long period;
	private final AtomicLong now;

	private SystemClock(long period) {
		this.period = period;
		this.now = new AtomicLong(System.currentTimeMillis());
		scheduleClockUpdating();
	}

	private static SystemClock instance() {
		return InstanceHolder.INSTANCE;
	}

	public static long now() {
		return instance().currentTimeMillis();
	}

	public static String nowDate() {
		return new Timestamp(instance().currentTimeMillis()).toString();
	}

	private void scheduleClockUpdating() {
		ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1, (r) -> {
			Thread thread = new Thread(r, "System Clock");
			thread.setDaemon(true);
			return thread;
		});

		scheduler.scheduleAtFixedRate(
				() -> now.set(System.currentTimeMillis()),
				period, period, TimeUnit.MICROSECONDS);
	}

	private long currentTimeMillis() {
		return now.get();
	}

	private static class InstanceHolder {
		public static final SystemClock INSTANCE = new SystemClock(1);
	}
}
