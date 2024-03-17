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
		switch (level.intValue()) {
			case ERROR_VALUE -> logger.error(msg);
			case WARN_VALUE -> logger.warn(msg);
			case INFO_VALUE -> logger.info(msg);
			case DEBUG_VALUE -> logger.debug(msg);
			case TRACE_VALUE -> logger.trace(msg);
			default -> {}
		}
	}

	@Override
	public void log(Level level, String msg, Object param1) {
		switch (level.intValue()) {
			case ERROR_VALUE -> logger.error(msg, param1);
			case WARN_VALUE -> logger.warn(msg, param1);
			case INFO_VALUE -> logger.info(msg, param1);
			case DEBUG_VALUE -> logger.debug(msg, param1);
			case TRACE_VALUE -> logger.trace(msg, param1);
			default -> {}
		}
	}

	@Override
	public void log(Level level, String msg, Object[] params) {
		switch (level.intValue()) {
			case ERROR_VALUE -> logger.error(msg, params);
			case WARN_VALUE -> logger.warn(msg, params);
			case INFO_VALUE -> logger.info(msg, params);
			case DEBUG_VALUE -> logger.debug(msg, params);
			case TRACE_VALUE -> logger.trace(msg, params);
			default -> {}
		}
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		switch (level.intValue()) {
			case ERROR_VALUE -> logger.error(msg, thrown);
			case WARN_VALUE -> logger.warn(msg, thrown);
			case INFO_VALUE -> logger.info(msg, thrown);
			case DEBUG_VALUE -> logger.debug(msg, thrown);
			case TRACE_VALUE -> logger.trace(msg, thrown);
			default -> {}
		}
	}
}
