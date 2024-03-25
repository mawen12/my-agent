package com.mawen.agent.plugin.redis;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.api.config.ConfigConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class RedisPlugin implements AgentPlugin {

	@Override
	public String getNamespace() {
		return ConfigConst.Namespace.REDIS;
	}

	@Override
	public String getDomain() {
		return ConfigConst.OBSERVABILITY;
	}
}
