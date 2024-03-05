package com.mawen.agent.metrics.model;

import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.plugin.tools.metrics.GaugeMetricModel;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@Data
@AllArgsConstructor
public class JVMMemoryGaugeMetricModel implements GaugeMetricModel {

	private Long bytesInit;
	private Long bytesUsed;
	private Long bytesCommitted;
	private Long bytesMax;

	@Override
	public Map<String, Object> toHashMap() {
		Map<String, Object> result = new HashMap<>();
		result.put("bytes-init", bytesInit);
		result.put("bytes-used", bytesUsed);
		result.put("bytes-committed", bytesCommitted);
		result.put("bytes-max", bytesMax);
		return result;
	}
}
