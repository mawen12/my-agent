package com.mawen.agent.report.util;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.report.OutputProperties;
import com.mawen.agent.report.metric.MetricProps;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class Utils {

	private Utils(){}

	public static boolean isOutputPropertiesChange(Map<String, String> changes) {
		List<String> relatedNames = Arrays.asList(ReportConfigConst.OUTPUT_SERVER_V2);
		return changes.keySet().stream().anyMatch(relatedNames::contains);
	}

	public static OutputProperties extractOutputProperties(Config configs) {
		return OutputProperties.newDefault(configs);
	}

	public static MetricProps extractMetricProps(IPluginConfig config, Config reportConfigs) {
		return MetricProps.newDefault(config, reportConfigs);
	}
}
