package com.mawen.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class ArgumentUtils {

	public static boolean needToUpdateArg(String argValue) {
		return argValue != null && !argValue.isEmpty();
	}

	public static boolean needToUpdateRollingArg(String enableRolling) {
		return enableRolling != null && !enableRolling.isEmpty() && Boolean.parseBoolean(enableRolling);
	}

	public static String getArgumentSingleValue(Map<String, List<String>> parsedArgs, String argName) {
		List<String> list = parsedArgs.get(argName);
		if (list == null) {
			return null;
		}

		if (list.isEmpty()) {
			return "";
		}

		return list.get(list.size() - 1);
	}

	public static List<String> getArgumentMultiValues(Map<String, List<String>> parsedArgs, String argName) {
		List<String> list = parsedArgs.get(argName);
		if (list == null) {
			return new ArrayList<>();
		}
		return list;
	}
}
