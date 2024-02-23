package com.mawen.agent.plugin.api.metric;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A timer metric which aggregates timing durations and provides duration statistics,
 * plus throughput statistics via {@link Meter}.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Timer extends Metric {

	/**
	 * A timing context.
	 */
	interface Context {

		/**
		 * Updates the timer with the difference between current and start time.
		 * Call to this method will not reset the start time.
		 * Multiple calls result in multiple updates.
		 *
		 * @return the elapsed time in nanoseconds
		 */
		long stop();

		/**
		 * Equivalent to calling {@link #stop()}.
		 */
		void close();
	}

	/**
	 * Adds a recorded duration.
	 *
	 * @param duration the length of the duration
	 * @param unit the scale unit of {@code duration}
	 */
	void update(long duration, TimeUnit unit);

	/**
	 * Adds a recorded duration.
	 *
	 * @param duration the {@link Duration} to add to the timer. Negative or zero value are ignored.
	 */
	void update(Duration duration);

	/**
	 * Times and records the duration of event.
	 *
	 * @param event a {@link Callable} whose {@link Callable#call()} method implements a process
	 *              whose duration should be timed
	 * @param <T> the type of the value returned by {@code event}
	 * @return the value returned by {@code event}
	 * @throws Exception if {@code event} throws an {@link Exception}
	 */
	<T> T time(Callable<T> event) throws Exception;

	/**
	 * Times and records the duration of event. Should not throw exceptions,
	 * for that use the {@link #time(Callable)} method.
	 *
	 * @param event a {@link Supplier} whose {@link Supplier#get()} method implements a process
	 *              whose duration should be timed
	 * @param <T> the type of the value returned by {@code event}
	 * @return the value returned by {@code event}
	 */
	<T> T timeSupplier(Supplier<T> event);

	/**
	 * Times and records the duration of event.
	 *
	 * @param event a {@link Runnable} whose {@link Runnable#run()} method implements a process
	 *              whose duration should be timed
	 */
	void time(Runnable event);

	/**
	 * Returns a new {@link Context}
	 *
	 * @return a new {@link Context}
	 * @see Context
	 */
	Timer.Context time();

	/**
	 * Returns the number of events which have been marked.
	 *
	 * @return the number of events which have been marked
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
	 * @return the mean rate at which events have occurred since the meter was created
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
	 * Returns a snapshot of the values.
	 *
	 * @return a snapshot of the values
	 */
	Snapshot getSnapshot();

	/**
	 * Returns the underlying Timer object or {@code null} if there is none.
	 * Here is a Timer objects: {@code com.codahale.metrics.Timer}
	 */
	Object unwrap();
}
