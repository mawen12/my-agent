package com.mawen.agent.plugin.tools.metrics;

import com.mawen.agent.plugin.api.metric.name.NameFactory;

/**
 * a {@link NameFactory} Supplier.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public interface NameFactorySupplier {

	/**
	 * new a NameFactory
	 *
	 * @return {@link NameFactory}
	 */
	NameFactory newInstance();
}
