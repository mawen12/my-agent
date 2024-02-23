package com.mawen.agent.plugin.api.metric;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Histogram extends Metric{

	void update(int value);

	void update(long value);

	long getCount();

	Snapshot getSnapshot();

	Object unwrap();
}
