package com.mawen.agent.plugin.utils.common;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ExceptionUtil {
	public static String getExceptionMessage(Throwable throwable) {
		return throwable.getMessage() != null ? throwable.getMessage() : throwable.toString();
	}

	public static String getExceptionStackTrace(Throwable e) {
		if (e == null) {
			return "";
		}
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		e.printStackTrace(pw);

		return sw.toString();
	}
}
