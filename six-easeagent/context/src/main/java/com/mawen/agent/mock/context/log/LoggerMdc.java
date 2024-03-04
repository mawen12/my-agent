package com.mawen.agent.mock.context.log;

import com.mawen.agent.plugin.api.logging.Mdc;
import lombok.AllArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AllArgsConstructor
public class LoggerMdc implements Mdc {

	private final com.mawen.agent.log4j2.api.Mdc mdc;

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
