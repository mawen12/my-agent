package com.mawen.agent.plugin.api.metric.name;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class MetricName {

	final MetricSubType metricSubType;
	final String key;
	final MetricType metricType;
	final Map<MetricField, MetricValueFetcher> valueFetcher;

	public MetricName(MetricSubType metricSubType, String key, MetricType metricType, Map<MetricField, MetricValueFetcher> valueFetcher) {
		this.metricSubType = metricSubType;
		this.key = key;
		this.metricType = metricType;
		this.valueFetcher = valueFetcher;
	}

	public MetricSubType getMetricSubType() {
		return metricSubType;
	}

	public String getKey() {
		return key;
	}

	public MetricType getMetricType() {
		return metricType;
	}

	public Map<MetricField, MetricValueFetcher> getValueFetcher() {
		return valueFetcher;
	}

	public static MetricName metricNameFor(String name) {
		return new MetricName(
				MetricSubType.valueFor(name.substring(0,2)),
				name.substring(3 ),
				MetricType.values()[Integer.parseInt(name.substring(2,3))],
				new HashMap<>());
	}

	public String name() {
		return metricSubType.getCode() + metricType.ordinal() + key;
	}
}
