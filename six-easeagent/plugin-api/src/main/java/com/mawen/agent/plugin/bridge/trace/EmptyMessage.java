package com.mawen.agent.plugin.bridge.trace;

import com.mawen.agent.plugin.api.trace.Message;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum EmptyMessage implements Message<Object> {
	INSTANCE;
	private static final Object OBJ_INSTANCE = new Object();

	@Override
	public Object get() {
		return OBJ_INSTANCE;
	}
}
