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
		switch (level.intValue()) {
			case ERROR_VALUE:
				return logger.isErrorEnabled();
			case WARN_VALUE:
				return logger.isWarnEnabled();
			case INFO_VALUE:
				return logger.isInfoEnabled();
			case DEBUG_VALUE:
				return logger.isDebugEnabled();
			case TRACE_VALUE:
				return logger.isTraceEnabled();
			default:
		}
		return false;
	}

	@Override
	public void log(Level level, String msg) {
		switch (level.intValue()) {
			case ERROR_VALUE:
				logger.error(msg);
				break;
			case WARN_VALUE:
				logger.warn(msg);
				break;
			case INFO_VALUE:
				logger.info(msg);
				break;
			case DEBUG_VALUE:
				logger.debug(msg);
				break;
			case TRACE_VALUE:
				logger.trace(msg);
				break;
			default:
		}
	}

	@Override
	public void log(Level level, String msg, Object param1) {
		switch (level.intValue()) {
			case ERROR_VALUE:
				logger.error(msg, param1);
				break;
			case WARN_VALUE:
				logger.warn(msg, param1);
				break;
			case INFO_VALUE:
				logger.info(msg, param1);
				break;
			case DEBUG_VALUE:
				logger.debug(msg, param1);
				break;
			case TRACE_VALUE:
				logger.trace(msg, param1);
				break;
			default:
		}
	}

	@Override
	public void log(Level level, String msg, Object[] params) {
		switch (level.intValue()) {
			case ERROR_VALUE:
				logger.error(msg, params);
				break;
			case WARN_VALUE:
				logger.warn(msg, params);
				break;
			case INFO_VALUE:
				logger.info(msg, params);
				break;
			case DEBUG_VALUE:
				logger.debug(msg, params);
				break;
			case TRACE_VALUE:
				logger.trace(msg, params);
				break;
			default:
		}
	}

	@Override
	public void log(Level level, String msg, Throwable thrown) {
		switch (level.intValue()) {
			case ERROR_VALUE:
				logger.error(msg, thrown);
				break;
			case WARN_VALUE:
				logger.warn(msg, thrown);
				break;
			case INFO_VALUE:
				logger.info(msg, thrown);
				break;
			case DEBUG_VALUE:
				logger.debug(msg, thrown);
				break;
			case TRACE_VALUE:
				logger.trace(msg, thrown);
				break;
			default:
		}
	}
}
