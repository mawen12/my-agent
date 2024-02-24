package com.mawen.agent.plugin.api.metric.name;

import java.sql.Time;

import com.mawen.agent.plugin.api.metric.Counter;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.Histogram;
import com.mawen.agent.plugin.api.metric.Meter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum MetricType {

	TimerType(Time.class),
	HistogramType(Histogram.class),
	MeterType(Meter.class),
	CounterType(Counter.class),
	GaugeType(Gauge.class),
	;

	<T> MetricType(Class<T> clazz) {

	}
}
