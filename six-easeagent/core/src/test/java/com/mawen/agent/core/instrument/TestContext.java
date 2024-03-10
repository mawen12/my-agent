package com.mawen.agent.core.instrument;

import com.mawen.agent.plugin.bridge.NoOpContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class TestContext extends NoOpContext.NoopContext {

	@Override
	public boolean isNoop() {
		return false;
	}
}
