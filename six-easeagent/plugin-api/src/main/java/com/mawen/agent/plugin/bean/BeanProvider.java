package com.mawen.agent.plugin.bean;

import com.mawen.agent.plugin.Ordered;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface BeanProvider extends Ordered {
	@Override
	default int order() {
		return BeanOrder.HIGH.getOrder();
	}

	enum BeanOrder {
		INIT(0, "init"),
		HIGH(20, "high"),
		METRIC_REGISTRY(200, "metric"),
		LOW(210, "low"),
		;

		private final int order;
		private final String name;

		BeanOrder(int order, String name) {
			this.order = order;
			this.name = name;
		}

		public int getOrder() {
			return order;
		}
	}
}
