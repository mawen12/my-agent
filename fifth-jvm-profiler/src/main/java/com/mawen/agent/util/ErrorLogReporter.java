package com.mawen.agent.util;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public interface ErrorLogReporter {

	void report(String message, Throwable exception);

	void close();
}
