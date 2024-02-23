package com.mawen.agent.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class JsonUtils {
	protected static ObjectMapper mapper = new ObjectMapper();

	static {
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
		mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

	public static String serialize(Object obj) {
		if (obj == null) {
			return "";
		}
		try {
			return mapper.writeValueAsString(obj);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(String.format("Failed to serialize %s (%s)", obj, obj.getClass()), e);
		}
	}

	public static <T> T deserialize(String content, Class<T> valueType) {
		try {
			return mapper.readValue(content, valueType);
		}
		catch (IOException e) {
			throw new RuntimeException(String.format("Failed to deserialize %s from json %s", valueType, content), e);
		}
	}

	// For example: JsonUtils.deserialize(responseBody, new TypeReference<List<Xxx>>() {})
	public static <T> T deserialize(String content, TypeReference<T> valueTypeRef) {
		try {
			return mapper.readValue(content, valueTypeRef);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(String.format("Failed to deserialize %s from json %s",valueTypeRef, content), e);
		}
	}
}
