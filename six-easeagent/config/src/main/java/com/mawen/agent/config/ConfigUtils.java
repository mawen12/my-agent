package com.mawen.agent.config;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ConfigUtils {

	public static <R> void bindProp(String name, Config configs, BiFunction<Config, String, R> func, Consumer<R> consumer) {
		bindProp(name, configs, func, consumer, null);
	}

	public static <R> void bindProp(String name, Config configs, BiFunction<Config, String, R> func, Consumer<R> consumer, R def) {
		Runnable process = () -> {
			R result = func.apply(configs, name);
			result = firstNotNull(result, def);
			if (result != null) {
				consumer.accept(result);
			}
		};
		process.run();
	}

	@SafeVarargs
	private static <R> R firstNotNull(R... ars) {
		for (R one : ars) {
			if (one != null) {
				return one;
			}
		}
		return null;
	}

	public static List<Map.Entry<String, String>> extractKVs(String prefix, JsonNode node) {
		List<Map.Entry<String, String>> rst = new LinkedList<>();
		if (node.isObject()) {
			Iterator<String> names = node.fieldNames();
			while (names.hasNext()) {
				String current = names.next();
				rst.addAll(extractKVs(join(prefix, current), node.path(current)));
			}
		}
		else if (node.isArray()) {
			int len = node.size();
			for (int i = 0; i < len; i++) {
				rst.addAll(extractKVs(join(prefix, i + ""), node.path(i)));
			}
		}
		else {
			rst.add(new AbstractMap.SimpleEntry<>(prefix, node.asText("")));
		}
		return rst;
	}

	private static String join(String prefix, String current) {
		return prefix == null ? current : ConfigConst.join(prefix, current);
	}

	public static boolean isGlobal(String namespace) {
		return ConfigConst.PLUGIN_GLOBAL.equals(namespace);
	}

	public static boolean isPluginConfig(String key) {
		return key != null && key.startsWith(ConfigConst.PLUGIN_PREFIX);
	}

	public static boolean isPluginConfig(String key, String domain, String namespace, String id) {
		return key != null && key.startsWith(ConfigConst.join(ConfigConst.PLUGIN, domain, namespace, id));
	}

	public static PluginProperty pluginProperty(String path) {
		String[] configs = path.split("\\" + ConfigConst.DELIMITER);
		if (configs.length < 5) {
			throw new ValidException(String.format("Property[%s] must be format: %s", path,
					ConfigConst.join(ConfigConst.PLUGIN, "<Domain>", "<Namespace>", "<Id>", "<Property>")));
		}

		for (int idOffsetEnd = 3; idOffsetEnd < configs.length - 1; idOffsetEnd++) {
			new PluginProperty(configs[1], configs[2],
					ConfigConst.join(Arrays.copyOfRange(configs, 3, idOffsetEnd)),
					ConfigConst.join(Arrays.copyOfRange(configs, idOffsetEnd + 1, configs.length)));
		}

		return new PluginProperty(configs[1], configs[2], configs[3],
				ConfigConst.join(Arrays.copyOfRange(configs, 4, configs.length)));
	}

	public static String buildPluginProperty(String domain, String namespace, String id, String property) {
		return String.format(ConfigConst.PLUGIN_FORMAT, domain, namespace, id, property);
	}

	/**
	 * extract config item with a fromPrefix to and convert the prefix to 'toPrefix' for configuration Compatibility
	 *
	 * @param cfg        config source map
	 * @param fromPrefix from
	 * @param toPrefix   to
	 * @return Extracted and converted KV map
	 */
	public static Map<String, String> extractAndConvertPrefix(Map<String, String> cfg, String fromPrefix, String toPrefix) {
		Map<String, String> convert = new HashMap<>();

		Set<String> keys = new HashSet<>();
		cfg.forEach((key, value) -> {
			if (key.startsWith(fromPrefix)) {
				keys.add(key);
				key = toPrefix + key.substring(fromPrefix.length());
				convert.put(key, value);
			}
		});

		// override, new configuration KV override previous KV
		convert.putAll(extractByPrefix(cfg, toPrefix));

		return convert;
	}

	/**
	 * Extract config items from config by prefix
	 *
	 * @param config config
	 * @param prefix prefix
	 * @return Extracted KV
	 */
	public static Map<String, String> extractByPrefix(Config config, String prefix) {
		return extractByPrefix(config.getConfigs(), prefix);
	}

	public static Map<String, String> extractByPrefix(Map<String, String> cfg, String prefix) {
		Map<String, String> extract = new TreeMap<>();

		// override, new configuration KV override previous KV
		cfg.forEach((key, value) -> {
			if (key.startsWith(prefix)) {
				extract.put(key, value);
			}
		});

		return extract;
	}

	private ConfigUtils() {
	}
}
