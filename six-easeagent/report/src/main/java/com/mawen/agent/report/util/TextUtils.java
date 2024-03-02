package com.mawen.agent.report.util;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class TextUtils {

	private TextUtils(){}

	public static boolean hasText(String content) {
		return content != null && content.trim().length() > 0;
	}

	public static boolean isEmpty(String value) {
		return !hasText(value);
	}
}
