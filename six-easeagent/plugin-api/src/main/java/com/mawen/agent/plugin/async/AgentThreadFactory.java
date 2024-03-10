package com.mawen.agent.plugin.async;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nullable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentThreadFactory implements ThreadFactory {
	protected static AtomicInteger createCount = new AtomicInteger(1);

	@Override
	public Thread newThread(@Nullable Runnable r) {
		var thread = new Thread(r, "agent-" + createCount.incrementAndGet());
		thread.setDaemon(true);
		return thread;
	}
}
