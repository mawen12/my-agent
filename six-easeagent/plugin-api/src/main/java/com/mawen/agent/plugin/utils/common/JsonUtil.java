package com.mawen.agent.plugin.utils.common;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class JsonUtil {
	static final ObjectMapper mapper = new ObjectMapper();
	private static final Logger logger = Agent.loggerFactory.getLogger(JsonUtil.class);

	static {
		mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	// expensive call
	public static String toJson(Object obj) {
		try {
			return mapper.writeValueAsString(obj);
		}
		catch (JsonProcessingException e) {
			logger.warn("data to json error: {}",e.getMessage());
			return null;
		}
	}

	public static Map<String, Object> toMap(String json) {
		try {
			return mapper.readValue(json, Map.class);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> List<T> toList(String json) {
		try {
			return mapper.readValue(json, new TypeReference<List<T>>() {});
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> T toObject(String json, TypeReference<T> valueTypeRef) {
		try {
			return mapper.readValue(json, valueTypeRef);
		}
		catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
}
