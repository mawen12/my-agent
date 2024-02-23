package com.mawen.agent.plugin.api.metric;

import com.mawen.agent.plugin.api.config.IPluginConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface MetricRegistrySupplier {

	MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags);
}
