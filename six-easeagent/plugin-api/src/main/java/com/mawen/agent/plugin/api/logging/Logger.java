package com.mawen.agent.plugin.api.logging;

/**
 * The interface extract from slf4j-api to avoid conflict with user's application's usage of slf4j
 * By wrapping log4j2, Logger is compatible with slf4j interface.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Logger {
	/**
	 * case-insensitive String constant used to retrieve the name of the root logger.
	 */
	String ROOT_LOGGER_NAME = "ROOT";

	/**
	 * Return the name of this {@code Logger} instance.
	 */
	String getName();

	/**
	 * Is the logger instance enabled for the TRACE level?
	 *
	 * @return true if this Logger is enabled for the TRACE level, false otherwise.
	 */
	boolean isTraceEnabled();

	/**
	 * Log a message at the TRACE level.
	 *
	 * @param msg the message string to be logged
	 */
	void trace(String msg);

	/**
	 * Log a message at the TRACE level according to the specified format and argument.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg the argument
	 */
	void trace(String format, Object arg);

	/**
	 * Log a message at the TRACE level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 */
	void trace(String format, Object arg1, Object arg2);

	/**
	 * Log a message at the TRACE level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 * However, this variant incurs the hidden (and relatively small) cost of creating an {@code Object[]} before
	 * invoking the method, event if this logger is disabled for TRACE.
	 * The variants taking {@link #trace(String, Object) one} and {@link #trace(String, Object, Object) two} arguments
	 * exist solely in order to avoid this hidden cost.
	 *
	 * @param format the format string
	 * @param arguments a list of 3 or more arguments
	 */
	void trace(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the TRACE level with an accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t the exception (throwable) to log
	 */
	void trace(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the DEBUG level?
	 *
	 * @return true if this logger is enabled for the DEBUG level, false otherwise.
	 */
	boolean isDebugEnabled();

	/**
	 * Log a message at the DEBUG level.
	 *
	 * @param msg the message string to be logged
	 */
	void debug(String msg);

	/**
	 * Log a message at the DEBUG level according to the specified format and argument.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg the argument
	 */
	void debug(String format, Object arg);

	/**
	 * Log a message at the DEBUG level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 */
	void debug(String format, Object arg1, Object arg2);

	/**
	 * Log a message at the DEBUG level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 * However, this variant incurs the hidden (and relatively small) cost of creating an {@code Object[]} before
	 * invoking the method, event if this logger is disabled for TRACE.
	 * The variants taking {@link #trace(String, Object) one} and {@link #trace(String, Object, Object) two} arguments
	 * exist solely in order to avoid this hidden cost.
	 *
	 * @param format the format string
	 * @param arguments a list of 3 or more arguments
	 */
	void debug(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the DEBUG level with an accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t the exception (throwable) to log
	 */
	void debug(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the INFO level?
	 *
	 * @return true if this logger is enabled for the DEBUG level, false otherwise.
	 */
	boolean isInfoEnabled();

	/**
	 * Log a message at the INFO level.
	 *
	 * @param msg the message string to be logged
	 */
	void info(String msg);

	/**
	 * Log a message at the INFO level according to the specified format and argument.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg the argument
	 */
	void info(String format, Object arg);

	/**
	 * Log a message at the INFO level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 */
	void info(String format, Object arg1, Object arg2);

	/**
	 * Log a message at the INFO level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 * However, this variant incurs the hidden (and relatively small) cost of creating an {@code Object[]} before
	 * invoking the method, event if this logger is disabled for TRACE.
	 * The variants taking {@link #trace(String, Object) one} and {@link #trace(String, Object, Object) two} arguments
	 * exist solely in order to avoid this hidden cost.
	 *
	 * @param format the format string
	 * @param arguments a list of 3 or more arguments
	 */
	void info(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the INFO level with an accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t the exception (throwable) to log
	 */
	void info(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the WARN level?
	 *
	 * @return true if this logger is enabled for the DEBUG level, false otherwise.
	 */
	boolean isWarnEnabled();

	/**
	 * Log a message at the WARN level.
	 *
	 * @param msg the message string to be logged
	 */
	void warn(String msg);

	/**
	 * Log a message at the WARN level according to the specified format and argument.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg the argument
	 */
	void warn(String format, Object arg);

	/**
	 * Log a message at the WARN level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 */
	void warn(String format, Object arg1, Object arg2);

	/**
	 * Log a message at the WARN level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 * However, this variant incurs the hidden (and relatively small) cost of creating an {@code Object[]} before
	 * invoking the method, event if this logger is disabled for TRACE.
	 * The variants taking {@link #trace(String, Object) one} and {@link #trace(String, Object, Object) two} arguments
	 * exist solely in order to avoid this hidden cost.
	 *
	 * @param format the format string
	 * @param arguments a list of 3 or more arguments
	 */
	void warn(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the WARN level with an accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t the exception (throwable) to log
	 */
	void warn(String msg, Throwable t);

	/**
	 * Is the logger instance enabled for the ERROR level?
	 *
	 * @return true if this logger is enabled for the DEBUG level, false otherwise.
	 */
	boolean isErrorEnabled();

	/**
	 * Log a message at the ERROR level.
	 *
	 * @param msg the message string to be logged
	 */
	void error(String msg);

	/**
	 * Log a message at the ERROR level according to the specified format and argument.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg the argument
	 */
	void error(String format, Object arg);

	/**
	 * Log a message at the ERROR level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 *
	 * @param format the format string
	 * @param arg1 the first argument
	 * @param arg2 the second argument
	 */
	void error(String format, Object arg1, Object arg2);

	/**
	 * Log a message at the ERROR level according to the specified format and arguments.
	 *
	 * <p>This form avoids superfluous object creation when the logger is disabled for the TRACE level.
	 * However, this variant incurs the hidden (and relatively small) cost of creating an {@code Object[]} before
	 * invoking the method, event if this logger is disabled for TRACE.
	 * The variants taking {@link #trace(String, Object) one} and {@link #trace(String, Object, Object) two} arguments
	 * exist solely in order to avoid this hidden cost.
	 *
	 * @param format the format string
	 * @param arguments a list of 3 or more arguments
	 */
	void error(String format, Object... arguments);

	/**
	 * Log an exception (throwable) at the ERROR level with an accompanying message.
	 *
	 * @param msg the message accompanying the exception
	 * @param t the exception (throwable) to log
	 */
	void error(String msg, Throwable t);
}
