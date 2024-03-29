package com.mawen.agent.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

import com.mawen.agent.config.CompatibilityConversion.MultipleConversion.SingleBuilder;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.ConfigConst.Namespace;
import com.mawen.agent.plugin.api.config.ConfigConst.Observability;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class CompatibilityConversion {
	private static final Logger LOGGER = LoggerFactory.getLogger(CompatibilityConversion.class);

	protected static final String[] REQUEST_NAMESPACE = new String[] {
			Namespace.HTTP_CLIENT,
			Namespace.OK_HTTP,
			Namespace.WEB_CLIENT,
			Namespace.FEIGN_CLIENT,
			Namespace.REST_TEMPLATE,
	};

	private static final Map<String, BiFunction<String, String, Conversion<?>>> KEY_TO_NAMESPACE;
	private static final Set<String> METRIC_SKIP;
	private static final Set<String> TRACING_SKIP;

	static {
		Map<String, BiFunction<String, String, Conversion<?>>> map = new HashMap<>();
		map.put(Observability.KEY_METRICS_ACCESS, SingleBuilder.observability(Namespace.ACCESS));

		map.put(Observability.KEY_METRICS_REQUEST, MultipleBuilder.observability(Arrays.asList(REQUEST_NAMESPACE)));
		map.put(Observability.KEY_METRICS_JDBC_STATEMENT, SingleBuilder.observability(Namespace.JDBC_STATEMENT));
		map.put(Observability.KEY_METRICS_JDBC_CONNECTION, SingleBuilder.observability(Namespace.JDBC_CONNECTION));
		map.put(Observability.KEY_METRICS_MD5_DICTIONARY, SingleBuilder.observability(Namespace.MD5_DICTIONARY));
		map.put(Observability.KEY_METRICS_RABBIT, SingleBuilder.observability(Namespace.RABBITMQ));
		map.put(Observability.KEY_METRICS_KAFKA, SingleBuilder.observability(Namespace.KAFKA));
		map.put(Observability.KEY_METRICS_CACHE, SingleBuilder.observability(Namespace.REDIS));
		map.put(Observability.KEY_METRICS_JVM_GC, null);
		map.put(Observability.KEY_METRICS_JVM_MEMORY, null);
		// todo may be need add metrics es

		map.put(Observability.KEY_TRACE_REQUEST, MultipleBuilder.observability(Arrays.asList(REQUEST_NAMESPACE)));
		map.put(Observability.KEY_TRACE_REMOTE_INVOKE, SingleBuilder.observability(Namespace.WEB_CLIENT));
		map.put(Observability.KEY_TRACE_KAFKA, SingleBuilder.observability(Namespace.KAFKA));
		map.put(Observability.KEY_TRACE_JDBC, SingleBuilder.observability(Namespace.JDBC));
		map.put(Observability.KEY_TRACE_CACHE, SingleBuilder.observability(Namespace.REDIS));
		map.put(Observability.KEY_TRACE_RABBIT, SingleBuilder.observability(Namespace.RABBITMQ));
		// todo may be need add trace es

		KEY_TO_NAMESPACE = map;

		TRACING_SKIP = new HashSet<>();
		TRACING_SKIP.add(Observability.KEY_COMM_ENABLED);
		TRACING_SKIP.add(Observability.KEY_COMM_SAMPLED_BY_QPS);
		TRACING_SKIP.add(Observability.KEY_COMM_OUTPUT);
		TRACING_SKIP.add(Observability.KEY_COMM_TAG);

		METRIC_SKIP = new HashSet<>();
		METRIC_SKIP.add(Observability.KEY_METRICS_JVM_GC);
		METRIC_SKIP.add(Observability.KEY_METRICS_JVM_MEMORY);
	}

	public static Map<String, String> transform(Map<String, String> oldConfigs) {
		var changedKeys = new HashMap<String, Object>();
		var newConfigs = new HashMap<String, String>();
		for (var entry : oldConfigs.entrySet()) {
			var conversion = transformConversion(entry.getKey());
			var changed = conversion.transform(newConfigs, entry.getValue());
			if (conversion.isChange()) {
				changedKeys.put(entry.getKey(), changed);
			}
		}

		if (changedKeys.isEmpty()) {
			return oldConfigs;
		}


		LOGGER.info("config key has transform: ");
		for (Map.Entry<String, Object> entry : changedKeys.entrySet()) {
			LOGGER.info("{} to {}", entry.getKey(), entry.getValue());
		}

		return newConfigs;
	}

	private static Conversion<?> transformConversion(String key) {
		if (key.startsWith("observability.metrics.")) {
			return metricConversion(key);
		}
		else if (key.startsWith("observability.tracings.")) {
			return tracingConversion(key);
		}
		return new FinalConversion(key, false);
	}

	private static Conversion<?> metricConversion(String key) {
		if (key.equals(Observability.METRICS_ENABLED)) {
			return new MultipleFinalConversion(Arrays.asList(
					new FinalConversion(Observability.METRICS_ENABLED, true),
					new FinalConversion(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_METRIC_ENABLED, true)
			), true);
		}

		return conversion(key, METRIC_SKIP, ConfigConst.PluginID.METRIC);
	}

	private static Conversion<?> tracingConversion(String key) {
		if (key.equals(Observability.TRACE_ENABLED)) {
			return new FinalConversion(ConfigConst.Plugin.OBSERVABILITY_GLOBAL_TRACING_ENABLED, true);
		}
		return conversion(key, TRACING_SKIP, ConfigConst.PluginID.TRACING);
	}

	private static Conversion<?> conversion(String key, Set<String> skipTest, String pluginId) {
		var keys = ConfigConst.split(key);
		if (keys.length < 4) {
			return new FinalConversion(key, false);
		}
		var key2 = keys[2];
		if (skipTest.contains(key2)) {
			return new FinalConversion(key, false);
		}
		var builder = KEY_TO_NAMESPACE.get(key2);
		if (builder == null) {
			builder = SingleBuilder.observability(key2);
		}
		var properties = new String[keys.length - 3];
		int index = 0;
		for (var i = 0; i < keys.length; i++) {
			properties[index++] = keys[i];
		}
		return builder.apply(pluginId, ConfigConst.join(properties));
	}

	interface Conversion<K> {
		K transform(Map<String, String> configs, String value);

		boolean isChange();
	}

	static record FinalConversion(String key, boolean change) implements Conversion<String> {

		@Override
		public String transform(Map<String, String> configs, String value) {
			configs.put(key, value);
			return key;
		}

		@Override
		public boolean isChange() {
			return change;
		}
	}

	static record MultipleFinalConversion(List<FinalConversion> conversions, boolean change) implements Conversion<List<String>> {

		@Override
		public List<String> transform(Map<String, String> configs, String value) {
			List<String> result = new ArrayList<>();
			for (FinalConversion conversion : conversions) {
				result.add(conversion.transform(configs, value));
			}
			return result;
		}

		@Override
		public boolean isChange() {
			return change;
		}
	}

	static record SingleConversion(String domain, String namespace, String id, String property) implements Conversion<String> {

		@Override
		public String transform(Map<String, String> configs, String value) {
			String key = ConfigUtils.buildPluginProperty(domain, namespace, id, property);
			configs.put(key, value);
			return key;
		}

		@Override
		public boolean isChange() {
			return true;
		}
	}

	static record MultipleConversion(String domain, List<String> namespaces, String id, String property) implements Conversion<List<String>> {

		@Override
		public List<String> transform(Map<String, String> configs, String value) {
			List<String> keys = new ArrayList<>();
			for (String namespace : namespaces) {
				String key = ConfigUtils.buildPluginProperty(domain, namespace, id, property);
				keys.add(key);
				configs.put(key, value);
			}
			return keys;
		}

		@Override
		public boolean isChange() {
			return true;
		}

		static record SingleBuilder(String domain, String namespace) implements BiFunction<String, String, Conversion<?>> {

			@Override
			public Conversion<?> apply(String id, String property) {
				return new SingleConversion(domain, namespace, id, property);
			}

			static SingleBuilder observability(String namespace) {
				return new SingleBuilder(ConfigConst.OBSERVABILITY, namespace);
			}
		}
	}

	static record MultipleBuilder(String domain, List<String> namespaces) implements BiFunction<String, String, Conversion<?>> {

		@Override
		public Conversion<?> apply(String id, String property) {
			return new MultipleConversion(domain, namespaces, id, property);
		}

		static MultipleBuilder observability(List<String> namespaces) {
			return new MultipleBuilder(ConfigConst.OBSERVABILITY, namespaces);
		}
	}
}
