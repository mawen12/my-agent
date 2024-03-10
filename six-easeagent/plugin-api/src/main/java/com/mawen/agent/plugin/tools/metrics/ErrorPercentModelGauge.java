package com.mawen.agent.plugin.tools.metrics;

import java.math.BigDecimal;
import java.util.Map;

import com.mawen.agent.plugin.utils.ImmutableMap;


public record ErrorPercentModelGauge(
		BigDecimal m1ErrorPercent,
		BigDecimal m5ErrorPercent,
		BigDecimal m15ErrorPercent) implements GaugeMetricModel {

	@Override
	public Map<String, Object> toHashMap() {
		return ImmutableMap.<String, Object>builder()
				.put("m1errpct", m1ErrorPercent)
				.put("m5errpct", m5ErrorPercent)
				.put("m15errpct", m15ErrorPercent)
				.build();
	}
}
