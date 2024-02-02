package com.mawen.agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public interface Profiler {

	long getIntervalMillis();

	void setReporter(Reporter reporter);

	void profile();

}
