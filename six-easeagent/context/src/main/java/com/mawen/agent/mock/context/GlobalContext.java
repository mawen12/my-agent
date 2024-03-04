package com.mawen.agent.mock.context;

import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Mdc;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Getter
@AllArgsConstructor
public class GlobalContext {

	private final Configs configs;
	private final MetricRegistrySupplier metric;
	private final ILoggerFactory loggerFactory;
	private Mdc mdc;
}
