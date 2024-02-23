package com.mawen.agent.plugin.api.logging;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface ILoggerFactory {

	/**
	 * Return a logger that logs to the Java agent log output.
	 *
	 * @return A log where messages can be written to the Java agent log file or console.
	 */
	Logger getLogger(String name);

	default Logger getLogger(Class<?> clazz) {
		return getLogger(clazz.getName());
	}
}
