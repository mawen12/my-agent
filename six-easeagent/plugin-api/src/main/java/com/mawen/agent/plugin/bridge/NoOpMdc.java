package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.logging.Mdc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/18
 */
public enum NoOpMdc implements Mdc {
	INSTANCE;

	@Override
	public void put(String key, String value) {
		// NOP
	}

	@Override
	public void remove(String key) {
		// NOP
	}

	@Override
	public String get(String key) {
		return null;
	}
}
