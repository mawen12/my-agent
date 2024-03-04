package com.mawen.agent.context.log;

import java.util.function.Function;

import com.mawen.agent.log4j2.api.AgentLogger;
import com.mawen.agent.plugin.api.logging.Logger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class LoggerImpl extends AgentLogger implements Logger {

	public static final Function<java.util.logging.Logger, LoggerImpl> LOGGER_SUPPLIER = LoggerImpl::new;

	public LoggerImpl(java.util.logging.Logger logger){
		super(logger);
	}
}
