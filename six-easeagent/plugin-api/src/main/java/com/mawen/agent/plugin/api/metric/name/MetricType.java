package com.mawen.agent.plugin.api.metric.name;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;
import com.mawen.agent.plugin.api.metric.Timer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum MetricType {

	TimerType(Timer.class),
	HistogramType(Histogram.class),
	MeterType(Meter.class),
	CounterType(Counter.class),
	GaugeType(Gauge.class),
	;

	<T> MetricType(Class<T> clazz) {

	}
}
