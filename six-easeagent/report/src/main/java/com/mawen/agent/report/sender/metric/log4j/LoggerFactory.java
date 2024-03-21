package com.mawen.agent.report.sender.metric.log4j;

import org.apache.logging.log4j.core.LoggerContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class LoggerFactory {

	private LoggerFactory(){}

	private static final LoggerContext loggerContext = new LoggerContext("ROOT");

	public static final LoggerContext getLoggerContext() {
		return loggerContext;
	}
}
