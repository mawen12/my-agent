package com.mawen.agent.plugin.report.metric;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;

/**
 * Metric plugin get reporter from metricReporterFactory
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface MetricReporterFactory {
	Reporter reporter(IPluginConfig config);
}
