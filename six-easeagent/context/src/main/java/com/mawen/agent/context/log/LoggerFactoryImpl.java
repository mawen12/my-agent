package com.mawen.agent.context.log;

import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.log4j2.api.AgentLoggerFactory;
import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Logger;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class LoggerFactoryImpl implements ILoggerFactory {

	private final AgentLoggerFactory<LoggerImpl> loggerFactory;

	@Override
	public Logger getLogger(String name) {
		return loggerFactory.getLogger(name);
	}

	public static LoggerFactoryImpl build() {
		AgentLoggerFactory<LoggerImpl> loggerFactory = LoggerFactory.newFactory(LoggerImpl.LOGGER_SUPPLIER, LoggerImpl.class);
		if (loggerFactory == null) {
			return null;
		}

		return new LoggerFactoryImpl(loggerFactory);
	}

	public AgentLoggerFactory<LoggerImpl> factory() {
		return loggerFactory;
	}
}
