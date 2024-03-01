package com.mawen.agent.plugin.elasticsearch;

import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.enums.Order;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class ElasticsearchRedirectPlugin implements AgentPlugin {

	@Override
	public String getNamespace() {
		return ConfigConst.Namespace.ELASTICSEARCH;
	}

	@Override
	public String getDomain() {
		return ConfigConst.INTEGRABILITY;
	}

	@Override
	public int order() {
		return Order.REDIRECT.getOrder();
	}
}
