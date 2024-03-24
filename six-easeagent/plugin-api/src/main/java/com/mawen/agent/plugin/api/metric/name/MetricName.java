package com.mawen.agent.plugin.api.metric.name;

import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public record MetricName(MetricSubType metricSubType, String key, MetricType metricType, Map<MetricField, MetricValueFetcher> valueFetcher) {

	public static MetricName metricNameFor(String name) {
		return new MetricName(
				MetricSubType.valueFor(name.substring(0,2)),
				name.substring(3 ),
				MetricType.values()[Integer.parseInt(name.substring(2,3))],
				Map.of());
	}

	public String name() {
		return metricSubType.getCode() + metricType.ordinal() + key;
	}
}
