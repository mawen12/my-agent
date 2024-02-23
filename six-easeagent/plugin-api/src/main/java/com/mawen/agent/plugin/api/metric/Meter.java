package com.mawen.agent.plugin.api.metric;

/**
 * A meter metric which measures mean throughput and one-, five-, and fifteen-minute
 * moving average throughput.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Meter extends Metric {

	/**
	 * Mark the occurrence of an event.
	 */
	void mark();

	/**
	 * Mark the occurrence of a given number of events.
	 *
	 * @param n the number of events
	 */
	void mark(long n);

	/**
	 * Returns the number of events which have been marked.
	 *
	 * @return the number of events which have been marked.
	 */
	long getCount();

	/**
	 * Returns the fifteen-minute moving average rate at which events have
	 * occurred since the meter was created.
	 *
	 * @return the fifteen-minute moving average rate at which events have
	 * occurred since the meter was created.
	 */
	double getFifteenMinuteRate();

	/**
	 * Returns the five-minute moving average rate at which events have
	 * occurred since the meter was created.
	 *
	 * @return the five-minute moving average rate at which events have
	 * occurred since the meter was created.
	 */
	double getFiveMinuteRate();

	/**
	 * Returns the mean rate at which events have occurred since the meter was created.
	 *
	 * @return the mean rate at which events have occurred since the meter was created.
	 */
	double getMeanRate();

	/**
	 * Returns the one-minute moving average rate at which events have
	 * occurred since the meter was created.
	 *
	 * @return the one-minute moving average rate at which events have
	 * occurred since the meter was created.
	 */
	double getOneMinuteRate();

	/**
	 * Returns the underlying Meter object or {@code null} if there is none.
	 * Here is a Meter objects: {@code com.codahale.metrics.Meter}
	 */
	Object unwrap();
}
