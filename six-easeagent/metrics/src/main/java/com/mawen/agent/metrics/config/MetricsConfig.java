package com.mawen.agent.metrics.config;

import java.util.concurrent.TimeUnit;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public interface MetricsConfig {

	boolean isEnabled();

	int getInterval();

	TimeUnit getIntervalUnit();

	void setIntervalChangeCallback(Runnable runnable);
}
