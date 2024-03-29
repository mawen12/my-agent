package com.mawen.agent.log4j2.api;

import java.util.function.Function;
import java.util.logging.Logger;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class AgentLogger implements com.mawen.agent.log4j2.Logger {
	public static final Function<Logger, AgentLogger> LOGGER_SUPPLIER = AgentLogger::new;

	private final Logger logger;

	public AgentLogger(Logger logger) {
		this.logger = logger;
	}

	public Logger getLogger() {
		return logger;
	}

	@Override
	public String getName() {
		return logger.getName();
	}

	@Override
	public boolean isTraceEnabled() {
		return logger.isLoggable(ILevel.TRACE);
	}

	@Override
	public void trace(String msg) {
		logger.log(ILevel.TRACE, msg);
	}

	@Override
	public void trace(String format, Object arg) {
		logger.log(ILevel.TRACE, format, arg);
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		logger.log(ILevel.TRACE, format, new Object[]{arg1, arg2});
	}

	@Override
	public void trace(String format, Object... arguments) {
		logger.log(ILevel.TRACE, format, arguments);
	}

	@Override
	public void trace(String msg, Throwable t) {
		logger.log(ILevel.TRACE, msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return logger.isLoggable(ILevel.DEBUG);
	}

	@Override
	public void debug(String msg) {
		logger.log(ILevel.DEBUG, msg);
	}

	@Override
	public void debug(String format, Object arg) {
		logger.log(ILevel.DEBUG, format, arg);
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		logger.log(ILevel.DEBUG, format, new Object[]{arg1, arg2});
	}

	@Override
	public void debug(String format, Object... arguments) {
		logger.log(ILevel.DEBUG, format, arguments);
	}

	@Override
	public void debug(String msg, Throwable t) {
		logger.log(ILevel.DEBUG, msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return logger.isLoggable(ILevel.INFO);
	}

	@Override
	public void info(String msg) {
		logger.log(ILevel.INFO, msg);
	}

	@Override
	public void info(String format, Object arg) {
		logger.log(ILevel.INFO, format, arg);
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		logger.log(ILevel.INFO, format, new Object[]{arg1, arg2});
	}

	@Override
	public void info(String format, Object... arguments) {
		logger.log(ILevel.INFO, format, arguments);
	}

	@Override
	public void info(String msg, Throwable t) {
		logger.log(ILevel.INFO, msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return logger.isLoggable(ILevel.WARN);
	}

	@Override
	public void warn(String msg) {
		logger.log(ILevel.WARN, msg);
	}

	@Override
	public void warn(String format, Object arg) {
		logger.log(ILevel.WARN, format, arg);
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		logger.log(ILevel.WARN, format, new Object[]{arg1, arg2});
	}

	@Override
	public void warn(String format, Object... arguments) {
		logger.log(ILevel.WARN, format, arguments);
	}

	@Override
	public void warn(String msg, Throwable t) {
		logger.log(ILevel.WARN, msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return logger.isLoggable(ILevel.ERROR);
	}

	@Override
	public void error(String msg) {
		logger.log(ILevel.ERROR, msg);
	}

	@Override
	public void error(String format, Object arg) {
		logger.log(ILevel.ERROR, format, arg);
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		logger.log(ILevel.ERROR, format, new Object[]{arg1, arg2});
	}

	@Override
	public void error(String format, Object... arguments) {
		logger.log(ILevel.ERROR, format, arguments);
	}

	@Override
	public void error(String msg, Throwable t) {
		logger.log(ILevel.ERROR, msg, t);
	}
}
