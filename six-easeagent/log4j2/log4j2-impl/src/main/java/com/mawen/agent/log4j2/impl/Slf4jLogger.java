package com.mawen.agent.log4j2.impl;


import java.util.logging.Level;

import org.slf4j.Logger;

import static com.mawen.agent.log4j2.api.ILevel.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class Slf4jLogger extends java.util.logging.Logger {
	private final Logger logger;

	public Slf4jLogger(Logger logger) {
		super("", null);
		this.logger = logger;
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public void info(String msg) {
		logger.info(msg);
	}

	@Override
	public boolean isLoggable(Level level) {
		return switch (level.intValue()) {
			case ERROR_VALUE -> logger.isErrorEnabled();
			case WARN_VALUE -> logger.isWarnEnabled();
			case INFO_VALUE -> logger.isInfoEnabled();
			case DEBUG_VALUE -> logger.isDebugEnabled();
			case TRACE_VALUE -> logger.isTraceEnabled();
			default -> false;
		};
	}

	@Override
	public void log(Level level, String msg) {
		doLog(level,msg,null);
	}

	@Override
	public void log(Level level, String msg, Object param1) {
		doLog(level,msg,param1);
	}

	@Override
	public void log(Level level, String msg, Object[] params) {
		doLog(level, msg, params);
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		doLog(level,msg,thrown);
	}

	protected void doLog(Level level, String msg, Object object) {
		switch (level.intValue()) {
			case ERROR_VALUE -> logger.error(msg, object);
			case WARN_VALUE -> logger.warn(msg, object);
			case INFO_VALUE -> logger.info(msg, object);
			case DEBUG_VALUE -> logger.debug(msg, object);
			case TRACE_VALUE -> logger.trace(msg, object);
			default -> {}
		}
	}
}
