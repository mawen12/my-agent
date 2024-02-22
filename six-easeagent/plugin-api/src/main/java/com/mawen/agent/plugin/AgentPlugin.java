package com.mawen.agent.plugin;

import com.mawen.agent.plugin.enums.Order;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface AgentPlugin extends Ordered {

	/**
	 * define the plugin name, avoid conflicts with others.
	 * it will be used as namespace when get configuration
	 */
	String getNamespace();

	/**
	 * define the plugin domain.
	 * it will be used to get configuration when loaded.
	 */
	String getDomain();

	default int order() {
		return Order.HIGH.getOrder();
	}

}
