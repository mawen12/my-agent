package com.mawen.agent.core.instrument;

import com.mawen.agent.plugin.AgentPlugin;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class TestPlugin implements AgentPlugin {

	@Override
	public String getNamespace() {
		return "test";
	}

	@Override
	public String getDomain() {
		return "observability";
	}
}
