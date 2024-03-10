package com.mawen.agent.core.utils;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.SneakyThrows;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class ServletUtils {

	private static final String UNKNOWN = "unknown";

	public static final String BEST_MATCHING_PATTERN_ATTRIBUTE = "org.springframework.web.servlet.HandlerMapping.bestMatchingPattern";

	public static String getHttpRouteAttributeFromRequest(HttpServletRequest request) {
		var httpRoute = request.getAttribute(BEST_MATCHING_PATTERN_ATTRIBUTE);
		return httpRoute != null ? httpRoute.toString() : "";
	}

	public static String getRemoteHost(HttpServletRequest request) {
		if (request == null) {
			return UNKNOWN;
		}

		var ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	}

	public static Map<String, String> getHeaders(HttpServletRequest request) {
		var headerNames = request.getHeaderNames();
		var map = new HashMap<String, String>();
		while (headerNames.hasMoreElements()) {
			var name = headerNames.nextElement();
			var value = request.getHeader(name);
			map.put(name, value);
		}
		return map;
	}

	@SneakyThrows
	public static Map<String, List<String>> getQueries(HttpServletRequest request) {
		var map = new HashMap<String, List<String>>();
		var queryString = request.getQueryString();
		if (queryString == null || queryString.isEmpty()) {
			return map;
		}

		var pairs = queryString.split("&");
		for (var pair : pairs) {
			var idx = pair.indexOf("=");
			var key = idx > 0 ? URLDecoder.decode(pair.substring(0, idx), "UTF-8") : pair;
			if (!map.containsKey(key)) {
				map.put(key, new LinkedList<>());
			}
			var value = idx > 0 && pair.length() > idx + 1 ? URLDecoder.decode(pair.substring(idx + 1), "UTF-8") : null;
			map.get(key).add(value);
		}

		return map;
	}

	private ServletUtils(){}
}
