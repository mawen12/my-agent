package com.mawen.agent.log4j2;

import java.util.function.Function;
import java.util.logging.Level;

import com.mawen.agent.log4j2.api.AgentLogger;
import com.mawen.agent.log4j2.api.AgentLoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class LoggerFactory {
	public static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(LoggerFactory.class.getName());
	protected static final AgentLoggerFactory<AgentLogger> FACTORY;

	static {
		AgentLoggerFactory<AgentLogger> factory = null;
		try {
			factory = AgentLoggerFactory.builder(classLoaderSupplier(), AgentLogger.LOGGER_SUPPLIER, AgentLogger.class)
					.build();
		}
		catch (Exception e) {
			LOGGER.log(Level.WARNING, String.format("build agent logger factory fail: %s<%s>.", e.getClass().getName(), e.getMessage()));
		}
		FACTORY = factory;
	}

	private static ClassLoaderSupplier classLoaderSupplier() {
		return new ClassLoaderSupplier.ClassLoaderHolder();
	}

	public static <N extends AgentLogger> AgentLoggerFactory<N> newFactory(Function<java.util.logging.Logger, N> loggerSupplier, Class<N> tClass) {
		if (FACTORY == null) {
			return null;
		}
		return FACTORY.newFactory(loggerSupplier, tClass);
	}

	public static Logger getLogger(String name) {
		if (FACTORY == null) {
			return new NoopLogger(name);
		}
		return FACTORY.getLogger(name);
	}

	public static Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}

	public static class NoopLogger implements Logger {
		private final String name;

		public NoopLogger(String name) {
			this.name = name;
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public boolean isTraceEnabled() {
			return false;
		}

		@Override
		public void trace(String msg) {
			// ignored
		}

		@Override
		public void trace(String format, Object arg) {
			// ignored
		}

		@Override
		public void trace(String format, Object arg1, Object arg2) {
			// ignored
		}

		@Override
		public void trace(String format, Object... arguments) {
			// ignored
		}

		@Override
		public void trace(String msg, Throwable t) {
			// ignored
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void debug(String msg) {
			// ignored
		}

		@Override
		public void debug(String format, Object arg) {
			// ignored
		}

		@Override
		public void debug(String format, Object arg1, Object arg2) {
			// ignored
		}

		@Override
		public void debug(String format, Object... arguments) {
			// ignored
		}

		@Override
		public void debug(String msg, Throwable t) {
			// ignored
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public void info(String msg) {
			// ignored
		}

		@Override
		public void info(String format, Object arg) {
			// ignored
		}

		@Override
		public void info(String format, Object arg1, Object arg2) {
			// ignored
		}

		@Override
		public void info(String format, Object... arguments) {
			// ignored
		}

		@Override
		public void info(String msg, Throwable t) {
			// ignored
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void warn(String msg) {
			// ignored
		}

		@Override
		public void warn(String format, Object arg) {
			// ignored
		}

		@Override
		public void warn(String format, Object arg1, Object arg2) {
			// ignored
		}

		@Override
		public void warn(String format, Object... arguments) {
			// ignored
		}

		@Override
		public void warn(String msg, Throwable t) {
			// ignored
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public void error(String msg) {
			// ignored
		}

		@Override
		public void error(String format, Object arg) {
			// ignored
		}

		@Override
		public void error(String format, Object arg1, Object arg2) {
			// ignored
		}

		@Override
		public void error(String format, Object... arguments) {
			// ignored
		}

		@Override
		public void error(String msg, Throwable t) {
			// ignored
		}
	}
}
