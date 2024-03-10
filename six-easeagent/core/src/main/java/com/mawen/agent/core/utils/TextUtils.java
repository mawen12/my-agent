package com.mawen.agent.core.utils;

import java.nio.charset.StandardCharsets;

import com.mawen.agent.plugin.utils.common.DataSize;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class TextUtils {

	public static String cutStrByDataSize(String str, DataSize size) {
		var now = str.getBytes(StandardCharsets.UTF_8);
		if (now.length <= size.toBytes()) {
			return str;
		}

		var temp = new String(now, 0, (int)size.toBytes(), StandardCharsets.UTF_8);
		var unstable = temp.charAt(temp.length() - 1);
		var old = str.charAt(temp.length() - 1);
		if (unstable != old) {
			return temp;
		}
		return new String(temp.toCharArray(), 0, temp.length() - 1);
	}

	public static boolean hasText(String val) {
		return val != null && val.trim().length() > 0;
	}

	private TextUtils() {

	}
}
