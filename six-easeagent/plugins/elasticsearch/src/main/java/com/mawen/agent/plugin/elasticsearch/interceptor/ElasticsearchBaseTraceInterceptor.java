package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.enums.Order;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public abstract class ElasticsearchBaseTraceInterceptor extends ElasticsearchBaseInterceptor {

	@Override
	public String getType() {
		return Order.TRACING.getName();
	}

	@Override
	public int order() {
		return Order.TRACING.getOrder();
	}
}
