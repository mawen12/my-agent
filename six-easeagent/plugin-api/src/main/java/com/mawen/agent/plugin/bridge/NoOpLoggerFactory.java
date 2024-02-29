package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.api.logging.Mdc;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NoOpLoggerFactory implements ILoggerFactory {
	public static final NoOpLoggerFactory INSTANCE = new NoOpLoggerFactory();
	public static final NoOpMdc NO_OP_MDC_INSTANCE = new NoOpMdc();

	@Override
	public Logger getLogger(String name) {
		return new NoOpLogger(name);
	}

	public static class NoOpMdc implements Mdc {
		@Override
		public void put(String key, String value) {
			// NOP
		}

		@Override
		public void remove(String key) {
			// NOP
		}

		@Override
		public String get(String key) {
			return null;
		}
	}

	public static class NoOpLogger implements Logger {
		private final String name;

		public NoOpLogger(String name) {
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
			// NOP
		}

		@Override
		public void trace(String format, Object arg) {
			// NOP
		}

		@Override
		public void trace(String format, Object arg1, Object arg2) {
			// NOP
		}

		@Override
		public void trace(String format, Object... arguments) {
			// NOP
		}

		@Override
		public void trace(String msg, Throwable t) {
			// NOP
		}

		@Override
		public boolean isDebugEnabled() {
			return false;
		}

		@Override
		public void debug(String msg) {
			// NOP
		}

		@Override
		public void debug(String format, Object arg) {
			// NOP
		}

		@Override
		public void debug(String format, Object arg1, Object arg2) {
			// NOP
		}

		@Override
		public void debug(String format, Object... arguments) {
			// NOP
		}

		@Override
		public void debug(String msg, Throwable t) {
			// NOP
		}

		@Override
		public boolean isInfoEnabled() {
			return false;
		}

		@Override
		public void info(String msg) {
			// NOP
		}

		@Override
		public void info(String format, Object arg) {
			// NOP
		}

		@Override
		public void info(String format, Object arg1, Object arg2) {
			// NOP
		}

		@Override
		public void info(String format, Object... arguments) {
			// NOP
		}

		@Override
		public void info(String msg, Throwable t) {
			// NOP
		}

		@Override
		public boolean isWarnEnabled() {
			return false;
		}

		@Override
		public void warn(String msg) {
			// NOP
		}

		@Override
		public void warn(String format, Object arg) {
			// NOP
		}

		@Override
		public void warn(String format, Object arg1, Object arg2) {
			// NOP
		}

		@Override
		public void warn(String format, Object... arguments) {
			// NOP
		}

		@Override
		public void warn(String msg, Throwable t) {
			// NOP
		}

		@Override
		public boolean isErrorEnabled() {
			return false;
		}

		@Override
		public void error(String msg) {
			// NOP
		}

		@Override
		public void error(String format, Object arg) {
			// NOP
		}

		@Override
		public void error(String format, Object arg1, Object arg2) {
			// NOP
		}

		@Override
		public void error(String format, Object... arguments) {
			// NOP
		}

		@Override
		public void error(String msg, Throwable t) {
			// NOP
		}
	}
}