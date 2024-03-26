package com.mawen.agent.plugin.api.metric.name;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class MetricName {
	private final MetricSubType metricSubType;
	private final String key;
	private final MetricType metricType;
	private final Map<MetricField, MetricValueFetcher> valueFetcher;

	public MetricName(MetricSubType metricSubType, String key, MetricType metricType, Map<MetricField, MetricValueFetcher> valueFetcher) {
		this.metricSubType = metricSubType;
		this.key = key;
		this.metricType = metricType;
		this.valueFetcher = valueFetcher;
	}

	public MetricSubType metricSubType() {
		return metricSubType;
	}

	public String key() {
		return key;
	}

	public MetricType metricType() {
		return metricType;
	}

	public Map<MetricField, MetricValueFetcher> valueFetcher() {
		return valueFetcher;
	}

	public static MetricName metricNameFor(String name) {
		return new MetricName(
				MetricSubType.valueFor(name.substring(0,2)),
				name.substring(3 ),
				MetricType.values()[Integer.parseInt(name.substring(2,3))],
				Maps.newHashMap());
	}

	public String name() {
		return metricSubType.getCode() + metricType.ordinal() + key;
	}
}
