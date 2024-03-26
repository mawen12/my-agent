package com.mawen.agent.mock.context.log;

import com.mawen.agent.plugin.api.logging.Mdc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class LoggerMdc implements Mdc {
	private final com.mawen.agent.log4j2.api.Mdc mdc;

	public LoggerMdc(com.mawen.agent.log4j2.api.Mdc mdc) {
		this.mdc = mdc;
	}

	public com.mawen.agent.log4j2.api.Mdc mdc() {
		return mdc;
	}

	@Override
	public void put(String key, String value) {
		mdc.put(key, value);
	}

	@Override
	public void remove(String key) {
		mdc.remove(key);
	}

	@Override
	public String get(String key) {
		return mdc.get(key);
	}
}
