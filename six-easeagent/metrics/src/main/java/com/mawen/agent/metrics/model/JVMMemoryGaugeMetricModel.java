package com.mawen.agent.metrics.model;

import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.plugin.tools.metrics.GaugeMetricModel;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */

public class JVMMemoryGaugeMetricModel implements GaugeMetricModel {
	private final Long bytesInit;
	private final Long bytesUsed;
	private final Long bytesCommitted;
	private final Long bytesMax;

	public JVMMemoryGaugeMetricModel(Long bytesInit, Long bytesUsed, Long bytesCommitted, Long bytesMax) {
		this.bytesInit = bytesInit;
		this.bytesUsed = bytesUsed;
		this.bytesCommitted = bytesCommitted;
		this.bytesMax = bytesMax;
	}

	public Long bytesInit() {
		return bytesInit;
	}

	public Long bytesUsed() {
		return bytesUsed;
	}

	public Long bytesCommitted() {
		return bytesCommitted;
	}

	public Long bytesMax() {
		return bytesMax;
	}

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
