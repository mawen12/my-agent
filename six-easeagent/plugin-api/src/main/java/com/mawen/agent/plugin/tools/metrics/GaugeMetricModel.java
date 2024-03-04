package com.mawen.agent.plugin.tools.metrics;

import java.util.Map;

/**
 * GaugeMetricModel is dedicated to producing gauge data with model.
 *
 * <p>Each gauge metric must to produce a object which implement {@link GaugeMetricModel}
 * in our metric collecting framework.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public interface GaugeMetricModel {

	/**
	 * Returning gauge data and its label.
	 *
	 * @return a map which contains field-value pairs of gauge data to serialize JSON data.
	 */
	Map<String, Object> toHashMap();
}
