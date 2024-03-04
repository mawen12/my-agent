package com.mawen.agent.plugin.tools.metrics;

import java.util.Map;

import com.mawen.agent.plugin.utils.ImmutableMap;
import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Builder
@Data
public class LastMinutesCounterGauge implements GaugeMetricModel{
	private final long m1Count;
	private final long m5Count;
	private final long m15Count;
	private final String prefix;

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
