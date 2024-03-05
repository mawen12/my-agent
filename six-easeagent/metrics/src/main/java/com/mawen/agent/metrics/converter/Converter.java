package com.mawen.agent.metrics.converter;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.Timer;

/**
 * Converter is dedicated to converting metrics object to
 * a serializable <b>HashMap</b> according to * metric scheme definition
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public interface Converter {

	List<Map<String, Object>> convertMap(SortedMap<String, Gauge> gauges,
			SortedMap<String, Counter> counters,
			SortedMap<String, Histogram> histograms,
			SortedMap<String, Meter> meters,
			SortedMap<String, Timer> timers);
}
