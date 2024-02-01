package com.mawen.agent.util;

import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class AgentLogger {
	private static boolean debug = true;
	private static ErrorLogReporter errorLogReporter;

	private String prefix;

	public static AgentLogger getLogger(String name) {
		return new AgentLogger(name);
	}

	public static void setDebug(boolean enableDebug) {
		debug = enableDebug;
	}

	public static void setErrorLogReporter(ErrorLogReporter reporter) {
		errorLogReporter = reporter;
	}

	public AgentLogger(String name) {
		if (name == null) {
			this.prefix = "";
		} else {
			this.prefix = name + ": ";
		}
	}

	public void log(String msg) {
		info(msg);
	}

	public void info(String msg) {
		System.out.println(System.currentTimeMillis() + " " + prefix + msg);
	}

	public void debug(String msg) {
		if (AgentLogger.debug) {
			info(msg);
		}
	}

	public void warn(String msg) {
		try {
			System.out.println("[WARNING]" + System.currentTimeMillis() + " " + prefix + msg);

			if (AgentLogger.errorLogReporter != null) {
				AgentLogger.errorLogReporter.report(msg, null);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void warn(String msg, Throwable ex) {
		try {
			System.out.println("[WARNING]" + System.currentTimeMillis() + " " + prefix + msg + " " + ExceptionUtils.getStackTrace(ex));

			if (AgentLogger.errorLogReporter != null) {
				AgentLogger.errorLogReporter.report(msg, ex);
			}
		}
		catch (Throwable executionException) {
			executionException.printStackTrace();
		}
	}

	// 在关闭时特别处理日志，因为我们不能依赖 kafka 来记录这些信息
	public void logShutdownMessage(String msg) {
		// 有时，控制台中输出的 Spark 日志似乎并未完全收集，因此记录到错误输出，以确保我们可以捕获关闭钩子的执行
		// 这是为了帮助调试错误当关闭钩子并未执行
		String log = System.currentTimeMillis() + " " + prefix + msg;
		System.out.println(log);
		System.err.println(log);
	}

}
