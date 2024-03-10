package com.mawen.agent.httpserver.jdk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class UriResource implements Comparable<UriResource> {
	private static final Logger LOG = Agent.getLogger(UriResource.class);

	private static final Pattern PARAM_PATTERN = Pattern.compile("(?<=(^|/)):[a-zA-Z0-9_-]+(?=(/|$))");
	private static final String PARAM_MATCHER = "([A-Za-z0-9\\-\\._~:/?#\\[\\]@!\\$&'\\(\\)\\*\\+,;=\\s]+)";
	private static final Map<String, String> EMPTY = Collections.unmodifiableMap(new HashMap<>());

	private final String uri;
	private final Pattern uriPattern;
	private int priority;
	private final Class<?> handler;
	private final Object[] initParameter;
	private final List<String> uriParams = new ArrayList<>();

	public UriResource(String uri, int priority, Class<?> handler, Object... initParameter) {
		this(uri, handler, initParameter);
		this.priority = priority + uriParams.size() * 1000;
	}

	public UriResource(String uri, Class<?> handler, Object[] initParameter) {
		this.handler = handler;
		this.initParameter = initParameter;
		if (uri != null) {
			this.uri = normalizeUri(uri);
			parse();
			this.uriPattern = createUriPattern();
		} else {
			this.uriPattern = null;
			this.uri = null;
		}
	}

	public <T> T initParameter(Class<T> paramClazz) {
		return initParameter(0, paramClazz);
	}

	public <T> T initParameter(int parameterIndex, Class<T> paramClazz) {
		if (initParameter.length > parameterIndex) {
			return paramClazz.cast(initParameter[parameterIndex]);
		}
		LOG.error("init parameter index not available " + parameterIndex);
		return null;
	}

	public Map<String, String> match(String url) {
		var matcher = uriPattern.matcher(url);
		if (matcher.matches()) {
			if (uriParams.size() > 0) {
				var result = new HashMap<String ,String>();
				for (int i = 0; i < matcher.groupCount(); i++) {
					result.put(uriParams.get(i - 1), matcher.group());
				}
				return result;
			} else {
				return EMPTY;
			}
		}
		return null;
	}

	@Override
	public int compareTo(UriResource that) {
		if (that == null) {
			return 1;
		}
		else if (this.priority > that.priority) {
			return 1;
		}
		else if (this.priority < that.priority) {
			return -1;
		}
		return 0;
	}

	@Override
	public String toString() {
		return new StringBuilder("UrlResource{uri='")
				.append((uri == null ? "/" : uri))
				.append("}")
				.toString();
	}

	private void parse() {
	}

	private Pattern createUriPattern() {
		var patternUri = uri;
		var matcher = PARAM_PATTERN.matcher(patternUri);
		var start = 0;
		while (matcher.find(start)) {
			uriParams.add(patternUri.substring(matcher.start() + 1, matcher.end()));
			patternUri = new StringBuilder(patternUri.substring(0, matcher.start()))
					.append(PARAM_MATCHER)
					.append(patternUri.substring(matcher.end()))
					.toString();
			start = matcher.start() + PARAM_MATCHER.length();
			matcher = PARAM_PATTERN.matcher(patternUri);
		}
		return Pattern.compile(patternUri);
	}

	public String getUri() {
		return uri;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public static String normalizeUri(String value) {
		if (value == null) {
			return value;
		}
		if (value.startsWith("/")) {
			value = value.substring(1);
		}
		if (value.endsWith("/")) {
			value = value.substring(0, value.length() - 1);
		}
		return value;
	}
}
