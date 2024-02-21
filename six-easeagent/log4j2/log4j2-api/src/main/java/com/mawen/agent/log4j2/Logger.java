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

	boolean isDebugEnabled();

	void debug(String msg);

	void debug(String format, Object arg);

	void debug(String format, Object arg1, Object arg2);

	void debug(String format, Object... arguments);

	void debug(String msg, Throwable t);

	boolean isInfoEnabled();

	void info(String msg);

	void info(String format, Object arg);

	void info(String format, Object arg1, Object arg2);

	void info(String format, Object... arguments);

	void info(String msg, Throwable t);

	boolean isWarnEnabled();

	void warn(String msg);

	void warn(String format, Object arg);

	void warn(String format, Object arg1, Object arg2);

	void warn(String format, Object... arguments);

	void warn(String msg, Throwable t);

	boolean isErrorEnabled();

	void error(String msg);

	void error(String format, Object arg);

	void error(String format, Object arg1, Object arg2);

	void error(String format, Object... arguments);

	void error(String msg, Throwable t);
}
