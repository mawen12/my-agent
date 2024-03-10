package com.mawen.agent.plugin.tools.metrics;

import java.util.Map;

import com.mawen.agent.plugin.utils.ImmutableMap;


public record LastMinutesCounterGauge(
		long m1Count,
		long m5Count,
		long m15Count,
		String prefix
) implements GaugeMetricModel{

	@Override
	public Map<String, Object> toHashMap() {
		String px =this.prefix == null ? "" : this.prefix;
		return ImmutableMap.<String, Object>builder()
				.put(px + "m1cnt", m1Count)
				.put(px + "m5cnt", m5Count)
				.put(px + "m15cnt", m15Count)
				.build();
	}
}
