package com.mawen.agent.plugin.api.metric;

import java.util.Map;

/**
 * A registry of metric interface.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface MetricRegistry {

	/**
	 * Removes the metrics with the given name.
	 *
	 * @param name the name of the metric
	 * @return whether or not the metric was removed
	 */
	boolean remove(String name);

	/**
	 * get all metrics
	 *
	 * @return Map<String, Metric>
	 */
	Map<String, Metric> getMetrics();

	/**
	 * Return the {@link Meter} registered under this name; or create and register
	 * a new {@link Meter} if none is registered.
	 *
	 * @param name the name of the metric
	 * @return a new or pre-existing {@link Meter}
	 */
	Meter meter(String name);

	/**
	 * Return the {@link Counter} registered under this name; or create and register
	 * a new {@link Counter} if none is registered.
	 *
	 * @param name the name of the metric
	 * @return a new or pre-existing {@link Counter}
	 */
	Counter counter(String name);

	/**
	 * Return the {@link Gauge} registered under this name; or create and register
	 * a new {@link Gauge} if none is registered.
	 *
	 * @param name the name of the metric
	 * @return a new or pre-existing {@link Gauge}
	 */
	Gauge gauge(String name);

	/**
	 * Return the {@link Histogram} registered under this name; or create and register
	 * a new {@link Histogram} if none is registered.
	 *
	 * @param name the name of the metric
	 * @return a new or pre-existing {@link Histogram}
	 */
	Histogram histogram(String name);

	/**
	 * Return the {@link Timer} registered under this name; or create and register
	 * a new {@link Timer} if none is registered.
	 *
	 * @param name the name of the metric
	 * @return a new or pre-existing {@link Timer}
	 */
	Timer timer(String name);
}
