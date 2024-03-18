package com.mawen.agent.log4j2;

/**
 * 通过包装从 slf4j-api 提取的接口，避免和用户应用中的 slf4j 冲突.
 * Logger 适配 slf4j 接口.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/21
 */
public interface Logger {

	String getName();

	boolean isTraceEnabled();

	void trace(String msg);

	void trace(String format, Object arg);

	void trace(String format, Object arg1, Object arg2);

	void trace(String format, Object... arguments);

	void trace(String msg, Throwable t);

	default void traceIfEnabled(String msg) {
		if (isTraceEnabled()) {
			trace(msg);
		}
	}

	default void traceIfEnabled(String format, Object arg) {
		if (isTraceEnabled()) {
			trace(format, arg);
		}
	}

	default void traceIfEnabled(String format, Object arg1, Object arg2) {
		if (isTraceEnabled()) {
			trace(format, arg1, arg2);
		}
	}

	default void traceIfEnabled(String format, Object... arguments) {
		if (isTraceEnabled()) {
			trace(format, arguments);
		}
	}

	default void traceIfEnabled(String msg, Throwable t) {
		if (isTraceEnabled()) {
			trace(msg, t);
		}
	}

	boolean isDebugEnabled();

	void debug(String msg);

	void debug(String format, Object arg);

	void debug(String format, Object arg1, Object arg2);

	void debug(String format, Object... arguments);

	void debug(String msg, Throwable t);

	default void debugIfEnabled(String msg) {
		if (isDebugEnabled()) {
			debug(msg);
		}
	}

	default void debugIfEnabled(String format, Object arg) {
		if (isDebugEnabled()) {
			debug(format, arg);
		}
	}

	default void debugIfEnabled(String format, Object arg1, Object arg2) {
		if (isDebugEnabled()) {
			debug(format, arg1, arg2);
		}
	}

	default void debugIfEnabled(String format, Object... arguments) {
		if (isDebugEnabled()) {
			debug(format, arguments);
		}
	}

	default void debugIfEnabled(String msg, Throwable t) {
		if (isDebugEnabled()) {
			debug(msg, t);
		}
	}

	boolean isInfoEnabled();

	void info(String msg);

	void info(String format, Object arg);

	void info(String format, Object arg1, Object arg2);

	void info(String format, Object... arguments);

	void info(String msg, Throwable t);

	default void infoIfEnabled(String msg) {
		if (isInfoEnabled()) {
			info(msg);
		}
	}

	default void infoIfEnabled(String format, Object arg) {
		if (isInfoEnabled()) {
			info(format, arg);
		}
	}

	default void infoIfEnabled(String format, Object arg1, Object arg2) {
		if (isInfoEnabled()) {
			info(format, arg1, arg2);
		}
	}

	default void infoIfEnabled(String format, Object... arguments) {
		if (isInfoEnabled()) {
			info(format, arguments);
		}
	}

	default void infoIfEnabled(String msg, Throwable t) {
		if (isInfoEnabled()) {
			info(msg, t);
		}
	}

	boolean isWarnEnabled();

	void warn(String msg);

	void warn(String format, Object arg);

	void warn(String format, Object arg1, Object arg2);

	void warn(String format, Object... arguments);

	void warn(String msg, Throwable t);

	default void warnIfEnabled(String msg) {
		if (isWarnEnabled()) {
			warn(msg);
		}
	}

	default void warnIfEnabled(String format, Object arg) {
		if (isWarnEnabled()) {
			warn(format, arg);
		}
	}

	default void warnIfEnabled(String format, Object arg1, Object arg2) {
		if (isWarnEnabled()) {
			warn(format, arg1, arg2);
		}
	}

	default void warnIfEnabled(String format, Object... arguments) {
		if (isWarnEnabled()) {
			warn(format, arguments);
		}
	}

	default void warnIfEnabled(String msg, Throwable t) {
		if (isWarnEnabled()) {
			warn(msg, t);
		}
	}

	boolean isErrorEnabled();

	void error(String msg);

	void error(String format, Object arg);

	void error(String format, Object arg1, Object arg2);

	void error(String format, Object... arguments);

	void error(String msg, Throwable t);

	default void errorIfEnabled(String msg) {
		if (isErrorEnabled()) {
			error(msg);
		}
	}

	default void errorIfEnabled(String format, Object arg) {
		if (isErrorEnabled()) {
			error(format, arg);
		}
	}

	default void errorIfEnabled(String format, Object arg1, Object arg2) {
		if (isErrorEnabled()) {
			error(format, arg1, arg2);
		}
	}

	default void errorIfEnabled(String format, Object... arguments) {
		if (isErrorEnabled()) {
			error(format, arguments);
		}
	}

	default void errorIfEnabled(String msg, Throwable t) {
		if (isErrorEnabled()) {
			error(msg, t);
		}
	}
}
