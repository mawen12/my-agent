package com.mawen.agent.plugin.api.metric;

/**
 * A gauge metrics is an instantaneous reading of a particular value.
 * To instrument a queue's depth.
 * <p>
 * for example:
 * <pre>{@code
 * 	final Queue<String> queue = new ConcurrentLinkedQueue<String>();
 * 	final Gauge<Integer> queueDepth = new Gauge<>(){
 * 		public Integer getValue(){
 * 		 	return queue.size();
 *        }
 *    };
 * }</pre>
 * </p>
 *
 * @param <T> the type of the metric's value
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Gauge<T> extends Metric {

	/**
	 * Returns the metric's current value
	 *
	 * @return the metric's current value
	 */
	T getValue();

}
