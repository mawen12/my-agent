package com.mawen.agent.plugin.async;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ScheduleHelper {
	public static final ScheduleHelper DEFAULT = new ScheduleHelper();

	private final ThreadFactory threadFactory = new AgentThreadFactory();
	private ScheduledExecutorService scheduleService = Executors.newSingleThreadScheduledExecutor(threadFactory);

	public void nonStopExecute(int initialDelay, int delay, Runnable command) {
		Executors.newSingleThreadScheduledExecutor(threadFactory)
				.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.SECONDS);
	}

	public void execute(int initialDelay, int delay, Runnable command) {
		this.scheduleService.scheduleWithFixedDelay(command, initialDelay, delay, TimeUnit.SECONDS);
	}

	public void shutdown() {
		this.scheduleService.shutdown();
	}
}
