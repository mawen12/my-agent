package com.mawen.agent.mock.context;

import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Mdc;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */

public class GlobalContext {
	private final Configs configs;
	private final MetricRegistrySupplier metric;
	private final ILoggerFactory loggerFactory;
	private final Mdc mdc;

	public GlobalContext(Configs configs, MetricRegistrySupplier metric, ILoggerFactory loggerFactory, Mdc mdc) {
		this.configs = configs;
		this.metric = metric;
		this.loggerFactory = loggerFactory;
		this.mdc = mdc;
	}

	public Configs configs() {
		return configs;
	}

	public MetricRegistrySupplier metric() {
		return metric;
	}

	public ILoggerFactory loggerFactory() {
		return loggerFactory;
	}

	public Mdc mdc() {
		return mdc;
	}
}
