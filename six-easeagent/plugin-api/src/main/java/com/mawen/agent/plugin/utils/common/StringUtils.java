package com.mawen.agent.plugin.utils.common;

import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class StringUtils {

	private static final String[] EMPTY_STRING_ARRAY = {};

	private static final String WINDOWS_FOLDER_SEPARATOR = "\\";

	private static final String FOLDER_SEPARATOR = "/";

	private static final char FOLDER_SEPARATOR_CHAR = '/';

	private static final String TOP_PATH = "..";

	private static final String CURRENT_PATH = ".";


	private StringUtils() {
	}

	/**
	 * <p>If the first one is empty, return the alternative value</p>
	 *
	 * @param first     first
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
		}
		else {
			return origin.substring(0, idx + 1) + suffix;
		}
	}

	/**
	 *
	 * @since 0.0.2-SNAPSHOT
	 */
	public static String cleanPath(String path) {
		if (isEmpty(path)) {
			return path;
		}

		String normalizedPath =  replace(path, WINDOWS_FOLDER_SEPARATOR, FOLDER_SEPARATOR);
		String pathToUse = normalizedPath;

		if (pathToUse.indexOf('.') == -1) {
			return path;
		}

		int prefixIndex = pathToUse.indexOf(":");
		String prefix = "";
		if (prefixIndex != -1) {
			prefix = pathToUse.substring(0, prefixIndex + 1);
			if (prefix.contains(FOLDER_SEPARATOR)) {
				prefix = "";
			}
			else {
				pathToUse = pathToUse.substring(prefixIndex + 1);
			}
		}
		if (pathToUse.startsWith(FOLDER_SEPARATOR)) {
			prefix = prefix + FOLDER_SEPARATOR;
			pathToUse = pathToUse.substring(1);
		}

		String[] pathArray = delimitedListToStringArray(pathToUse, FOLDER_SEPARATOR);
		Deque<String> pathElements = new ArrayDeque<>(pathArray.length);
		int tops = 0;

		for (int i = pathArray.length - 1; i >= 0; i--) {
			String element = pathArray[i];
			if (CURRENT_PATH.equals(element)) {
			}
			else if (TOP_PATH.equals(element)) {
				tops++;
			}
			else {
				if (tops > 0) {
					tops--;
				}
				else {
					pathElements.addFirst(element);
				}
			}
		}

		if (pathArray.length == pathElements.size()) {
			return normalizedPath;
		}
		for (int i = 0; i < tops; i++) {
			pathElements.addFirst(TOP_PATH);
		}
		if (pathElements.size() == 1 && pathElements.getLast().isEmpty() && !prefix.endsWith(FOLDER_SEPARATOR)) {
			pathElements.addFirst(CURRENT_PATH);
		}

		final String joined = String.join(FOLDER_SEPARATOR, pathElements);
		return prefix.isEmpty() ? joined : prefix + joined;
	}

	public static String replace(String inString, String oldPattern, String newPattern) {
		if (isEmpty(inString) || isEmpty(oldPattern) || newPattern == null) {
			return inString;
		}
		int index = inString.indexOf(oldPattern);
		if (index == -1) {
			// no occurrence -> can return input as-is
			return inString;
		}

		int capacity = inString.length();
		if (newPattern.length() > oldPattern.length()) {
			capacity += 16;
		}
		StringBuilder sb = new StringBuilder(capacity);

		int pos = 0;  // our position in the old string
		int patLen = oldPattern.length();
		while (index >= 0) {
			sb.append(inString, pos, index);
			sb.append(newPattern);
			pos = index + patLen;
			index = inString.indexOf(oldPattern, pos);
		}

		// append any characters to the right of a match
		sb.append(inString, pos, inString.length());
		return sb.toString();
	}

	public static String[] delimitedListToStringArray(String str, String delimiter) {
		return delimitedListToStringArray(str, delimiter, null);
	}

	public static String[] delimitedListToStringArray(String str, String delimiter, String charsToDelete) {

		if (str == null) {
			return EMPTY_STRING_ARRAY;
		}
		if (delimiter == null) {
			return new String[] {str};
		}

		List<String> result = new ArrayList<>();
		if (delimiter.isEmpty()) {
			for (int i = 0; i < str.length(); i++) {
				result.add(deleteAny(str.substring(i, i + 1), charsToDelete));
			}
		}
		else {
			int pos = 0;
			int delPos;
			while ((delPos = str.indexOf(delimiter, pos)) != -1) {
				result.add(deleteAny(str.substring(pos, delPos), charsToDelete));
				pos = delPos + delimiter.length();
			}
			if (str.length() > 0 && pos <= str.length()) {
				// Add rest of String, but not in case of empty input.
				result.add(deleteAny(str.substring(pos), charsToDelete));
			}
		}
		return toStringArray(result);
	}

	public static String deleteAny(String inString, String charsToDelete) {
		if (isEmpty(inString) || isEmpty(charsToDelete)) {
			return inString;
		}

		int lastCharIndex = 0;
		char[] result = new char[inString.length()];
		for (int i = 0; i < inString.length(); i++) {
			char c = inString.charAt(i);
			if (charsToDelete.indexOf(c) == -1) {
				result[lastCharIndex++] = c;
			}
		}
		if (lastCharIndex == inString.length()) {
			return inString;
		}
		return new String(result, 0, lastCharIndex);
	}

	public static String[] toStringArray(Collection<String> collection) {
		return (!CollectionUtils.isEmpty(collection) ? collection.toArray(EMPTY_STRING_ARRAY) : EMPTY_STRING_ARRAY);
	}

	public static String[] toStringArray(Enumeration<String> enumeration) {
		return (enumeration != null ? toStringArray(Collections.list(enumeration)) : EMPTY_STRING_ARRAY);
	}

	public static String applyRelativePath(String path, String relativePath) {
		int separatorIndex = path.lastIndexOf(FOLDER_SEPARATOR_CHAR);
		if (separatorIndex != -1) {
			String newPath = path.substring(0, separatorIndex);
			if (!relativePath.startsWith(FOLDER_SEPARATOR)) {
				newPath += FOLDER_SEPARATOR_CHAR;
			}
			return newPath + relativePath;
		}
		else {
			return relativePath;
		}
	}
}
