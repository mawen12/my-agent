package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.Cleaner;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NoOpCleaner implements Cleaner {
	public static final NoOpCleaner INSTANCE = new NoOpCleaner();

	@Override
	public void close() {
		// NOP
	}
}
