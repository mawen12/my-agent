package com.mawen.agent.plugin.api.metric;

/**
 * An incrementing and decrementing counter metric.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Counter extends Metric {

	/**
	 * Increment the counter by one.
	 */
	void inc();

	/**
	 * Increment the counter by {@code n}.
	 *
	 * @param n the amount by which the counter will be increased
	 */
	void inc(long n);

	/**
	 * Decrement the counter by one.
	 */
	void dec();

	/**
	 * Decrement the counter by {@code n}
	 *
	 * @param n the amount by which the counter will be decreased
	 */
	void dec(long n);

	/**
	 * Returns the counter's current value.
	 *
	 * @return the counter's current value
	 */
	long getCount();

	/**
	 * Return the underlying Counter object or {@code null} if there is none.
	 * Here is a Counter objects: {@code com.codahale.metrics.Counter}
	 */
	Object unwrap();
}
