package com.mawen.agent.plugin.elasticsearch;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.api.config.ConfigConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class ElasticsearchPlugin implements AgentPlugin {

	@Override
	public String getNamespace() {
		return ConfigConst.Namespace.ELASTICSEARCH;
	}

	@Override
	public String getDomain() {
		return ConfigConst.OBSERVABILITY;
	}
}
