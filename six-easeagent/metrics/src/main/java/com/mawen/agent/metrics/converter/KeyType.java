package com.mawen.agent.metrics.converter;

/**
 * KeyType indicated how to fetch key value from which type metric of MetricRegistry.
 * fist we need to recognize what is key of data?
 *
 * <p>The key is a value which attached many metrics value with it, for example:
 *
 * <p>In <b>http-request</b>, we think the url is key, other metric value describe a special url properties,
 * in <b>jvm-memory</b> resource is key.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public enum KeyType {

	Timer,
	Gauge,
	Counter,
	Histogram,
	Meter;
}
