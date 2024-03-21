package com.mawen.agent.plugin.bridge.metric;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.bridge.NoOpReporter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public enum NoOpMetricsRegistrySupplier implements MetricRegistrySupplier {

	INSTANCE;

	@Override
	public MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
		return NoOpMetricsRegistry.INSTANCE;
	}

	@Override
	public Reporter reporter(IPluginConfig config) {
		return NoOpReporter.INSTANCE;
	}
}
