package com.mawen.agent.config.yaml;

import java.io.InputStream;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class YamlReader {
	private static final DumperOptions DUMPER_OPTIONS;

	static {
		DUMPER_OPTIONS = new DumperOptions();
		DUMPER_OPTIONS.setLineBreak(DumperOptions.LineBreak.getPlatformLineBreak());
	}

	private Map<String, Object> yaml;

	public YamlReader() {
		// ignored
	}

	public YamlReader load(InputStream in) {
		if (in != null) {
			yaml = new Yaml(DUMPER_OPTIONS).load(in);
		}
		return this;
	}

	public static YamlReader merge(YamlReader target, YamlReader source) {
		var targetMap = target.yaml;
		var sourceMap = source.yaml;

		merge(targetMap, sourceMap);

		var result = new YamlReader();
		result.yaml = new HashMap<>(targetMap);
		return result;
	}

	private static void merge(Map<String, Object> target, Map<String, Object> source) {
		source.forEach((key, value) -> {
			var existing = target.get(key);
			if (value instanceof Map && existing instanceof Map) {
				Map<String, Object> result = new LinkedHashMap<>((Map<String, Object>) existing);
				merge(result, (Map<String, Object>) value);
				target.put(key, result);
			} else {
				target.put(key, value);
			}
		});
	}

	public Map<String, String> compress() {
		if (Objects.isNull(yaml) || yaml.size() == 0) {
			return Collections.emptyMap();
		}

		final var keyStack = new LinkedList<String>();
		final var resultMap = new HashMap<String, String>();

		compress(yaml, keyStack, resultMap);

		return resultMap;
	}

	private void compress(Map<?, Object> result, Deque<String> keyStack, Map<String, String> resultMap) {
		result.forEach((k, v) -> {
			keyStack.addLast(String.valueOf(k));

			if (v instanceof Map) {
				compress((Map<?, Object>) v, keyStack, resultMap);
				keyStack.removeLast();
				return;
			}

			if (v instanceof List list) {
				var value = ((List<Object>) v).stream()
						.map(String::valueOf)
						.collect(Collectors.joining(","));

				resultMap.put(String.join(".", keyStack), value);
				keyStack.removeLast();
				return;
			}

			resultMap.put(String.join(".", keyStack),String.valueOf(v));
			keyStack.removeLast();
		});
	}

	public Map<String, Object> getYaml() {
		return yaml;
	}
}
