package com.mawen.agent.plugin.utils.common;

import java.nio.charset.StandardCharsets;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class StringUtils {

	private StringUtils(){}

	/**
	 * <p>If the first one is empty, return the alternative value</p>
	 *
	 * @param first first
	 * @param alternate alternative value
	 * @return String
	 */
	public static String noEmptyOf(String first, String alternate) {
		if (isEmpty(first)) {
			return alternate;
		}
		return first;
	}

	public static String noEmptyOf(String first, String alternate, String defaultValue) {
		if (isEmpty(first)) {
			noEmptyOf(alternate, defaultValue);
		}
		return first;
	}

	/**
	 * <p>Checks if a CharSequence is empty ("") or null.</p>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if the CharSequence is empty or null
	 */
	public static boolean isEmpty(final CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	/**
	 * <p>Checks if a CharSequence is not empty ("") and not null.</p>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} is the CharSequence is not empty and not null
	 */
	public static boolean isNotEmpty(final CharSequence cs) {
		return !isEmpty(cs);
	}

	/**
	 * <p>Checks if the CharSequence contains only Unicode digits</p>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if only contains digits, and is non-null
	 */
	public static boolean isNumeric(final CharSequence cs) {
		if (isEmpty(cs)) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>Checks if the CharSequence contains only Unicode digits or space ({@code ' '})</p>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if only contains digits or space.
	 */
	public static boolean isNumericSpace(final CharSequence cs) {
		if (cs == null) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isDigit(cs.charAt(i)) && cs.charAt(i) != ' ') {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>Checks if the CharSequence contains only whitespace.</p>
	 *
	 * @param cs the CharSequence to check, may be null
	 * @return {@code true} if only contains whitespace, and is non-null
	 */
	public static boolean isWhitespace(final CharSequence cs) {
		if (cs == null) {
			return false;
		}
		final int sz = cs.length();
		for (int i = 0; i < sz; i++) {
			if (!Character.isWhitespace(cs.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	public static String cutStrByDataSize(String str, DataSize size) {
		byte[] now = str.getBytes(StandardCharsets.UTF_8);
		if (now.length <= size.toBytes()) {
			return str;
		}
		String tmp = new String(now, 0, (int) size.toBytes(), StandardCharsets.UTF_8);
		// expected new str last char is equals to old in same position, if not equals, should drop last char
		char unstable = tmp.charAt(tmp.length() - 1);
		char old = str.charAt(tmp.length() - 1);
		if (unstable == old) {
			return tmp;
		}
		return new String(tmp.toCharArray(), 0, tmp.length() - 1);
	}

	public static boolean hasText(String val) {
		return val != null && val.trim().length() > 0;
	}

	public static String[] split(final String str, final String separatorChars) {
		if (isEmpty(str)) {
			return null;
		}
		return str.split(separatorChars);
	}

	public static String replaceSuffix(String origin, String suffix) {
		int idx = origin.lastIndexOf('.');
		if (idx == 0) {
			return suffix;
		} else {
			return origin.substring(0, idx + 1) + suffix;
		}
	}
}
