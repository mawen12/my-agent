package com.mawen.agent.report.sender.metric.log4j;

import java.util.function.Consumer;

import org.apache.logging.log4j.core.LogEvent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public interface TestableAppender {

	void setTestAppender(Consumer<LogEvent> logEventConsumer);
}
