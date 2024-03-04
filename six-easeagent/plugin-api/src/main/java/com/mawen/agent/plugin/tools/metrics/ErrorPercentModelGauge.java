package com.mawen.agent.plugin.tools.metrics;

import java.math.BigDecimal;
import java.util.Map;

import com.mawen.agent.plugin.utils.ImmutableMap;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Data
public class ErrorPercentModelGauge implements GaugeMetricModel{
	private BigDecimal m1ErrorPercent;
	private BigDecimal m5ErrorPercent;
	private BigDecimal m15ErrorPercent;


	@Override
	public Map<String, Object> toHashMap() {
		return ImmutableMap.<String, Object>builder()
				.put("m1errpct", m1ErrorPercent)
				.put("m5errpct", m5ErrorPercent)
				.put("m15errpct", m15ErrorPercent)
				.build();
	}
}
