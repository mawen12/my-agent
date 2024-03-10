package com.mawen.agent.plugin.api.otlp.common;

import java.util.concurrent.ConcurrentHashMap;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class SemanticKey {
	public static final String SCHEMA_URL = SemanticAttributes.SCHEMA_URL;
	public static final AttributeKey<String> THREAD_NAME = SemanticAttributes.THREAD_NAME;
	public static final AttributeKey<Long> THREAD_ID = SemanticAttributes.THREAD_ID;

	public static final AttributeKey<String> EXCEPTION_TYPE = SemanticAttributes.EXCEPTION_TYPE;
	public static final AttributeKey<String> EXCEPTION_MESSAGE = SemanticAttributes.EXCEPTION_MESSAGE;
	public static final AttributeKey<String> EXCEPTION_STACKTRACE = SemanticAttributes.EXCEPTION_STACKTRACE;

	private static final ConcurrentHashMap<String, AttributeKey<String>> keysMap = new ConcurrentHashMap<>();

	public static AttributeKey<String> stringKey(String key) {
		var vk = keysMap.get(key);
		return vk != null ? vk : keysMap.computeIfAbsent(key, AttributeKey::stringKey);
	}
}
