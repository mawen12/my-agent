package com.mawen.agent.plugin.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class ProgressFields {
	public static final String AGENT_PROGRESS_FORWARDED_HEADERS_CONFIG = "agent.progress.forwarded.headers";
	public static final String OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG = "observability.tracings.tag.response.headers";
	public static final String OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG = "observability.tracings.service.tags";
	private static volatile Fields responseHoldTagFields = build(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG, Collections.emptyMap());
	private static volatile Fields serviceTags = build(OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG, Collections.emptyMap());
	private static final Set<String> forwardHeaderSet = new HashSet<>();

	public static Set<String> getForwardHeaderSet() {
		return forwardHeaderSet;
	}

	public static boolean isProgressFields(String key) {
		return isForwardedHeader(key) || isResponseHoldTagKey(key) || isServerTags(key);
	}

	private static boolean isForwardedHeader(String key) {
		return AGENT_PROGRESS_FORWARDED_HEADERS_CONFIG.equals(key);
	}

	private static boolean isResponseHoldTagKey(String key) {
		return key.startsWith(OBSERVABILITY_TRACINGS_TAG_RESPONSE_HEADERS_CONFIG);
	}

	private static boolean isServerTags(String key) {
		return key.startsWith(OBSERVABILITY_TRACINGS_SERVICE_TAGS_CONFIG);
	}

	private static void buildForwardedHeaderSet(String value) {
		var split = StringUtils.split(value, ",");
		forwardHeaderSet.clear();
		if (split == null || split.length == 0) {
			return;
		}
		forwardHeaderSet.addAll(Arrays.asList(split));
	}

	private static void setResponseHoldTagFields(Map<String, String> fields) {
		responseHoldTagFields = responseHoldTagFields.rebuild(fields);
	}

	private static void setServiceTags(Map<String, String> tags) {
		serviceTags = serviceTags.rebuild(tags);
	}

	public static boolean isEmpty(String[] fields) {
		return fields == null || fields.length == 0;
	}

	public static String[] getResponseHoldTagFields() {
		return responseHoldTagFields.values;
	}

	public static Map<String, String> getServiceTags() {
		return serviceTags.keyValues;
	}

	private static Fields build(@Nonnull String keyPrefix, @Nonnull Map<String, String> map) {
		if (map.isEmpty()) {
			return new Fields(keyPrefix, Collections.emptySet(), Collections.emptyMap(), Collections.emptyMap());
		}
		Map<String, String> keyValues = new HashMap<>();
		for (var entry : map.entrySet()) {
			var key = entry.getKey().replace(keyPrefix, "");
			keyValues.put(key, entry.getValue());
		}
		return new Fields(keyPrefix, Collections.unmodifiableSet(new HashSet<>(map.values())), keyValues, map);
	}

	public static class Fields {
		private final String keyPrefix;
		private final String[] values;
		private final Map<String, String> keyValues;
		private final Map<String, String> map;

		private Fields(@Nonnull String keyPrefix, @Nonnull Set<String> fieldSet, @Nonnull Map<String, String> keyValues, @Nonnull Map<String, String> map) {
			this.keyPrefix = keyPrefix;
			this.values = fieldSet.toArray(new String[0]);
			this.keyValues = keyValues;
			this.map = map;
		}

		Fields rebuild(@Nonnull Map<String, String> map) {
			if (this.map.isEmpty()) {
				map.entrySet().removeIf(entry -> StringUtils.isEmpty(entry.getValue()));
				return build(keyPrefix, map);
			}
			var newMap = new HashMap<>(this.map);
			for (var entry : map.entrySet()) {
				if (StringUtils.isEmpty(entry.getValue())) {
					newMap.remove(entry.getKey());
				} else {
					newMap.put(entry.getKey(), entry.getValue());
				}
			}
			return build(keyPrefix, Collections.unmodifiableMap(newMap));
		}
	}
}
