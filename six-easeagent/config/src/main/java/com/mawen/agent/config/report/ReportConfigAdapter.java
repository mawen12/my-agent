package com.mawen.agent.config.report;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ReportConfigAdapter {
	private static final Logger LOGGER = Agent.loggerFactory.getLogger(ReportConfigAdapter.class);

	private ReportConfigAdapter() {
	}

	public static void convertConfig(Map<String, String> config) {

	}

	public static Map<String, String> extractMetricPluginConfig(Map<String, String> srcCfg) {
		// todo
	}

	private static Map<String, String> extractGlobalMetricConfig(Map<String, String> srcCfg) {
		final String prefix = join(ConfigConst.PLUGIN, ConfigConst.OBSERVABILITY, ConfigConst.PLUGIN_GLOBAL,
				ConfigConst.PluginID.METRIC);
		Map<String, String> global = new TreeMap<>();
		Map<String, String> extract = ConfigUtils.extractAndConvertPrefix(srcCfg, prefix, METRIC_SENDER);

		for (Map.Entry<String, String> e : extract.entrySet()) {
			if (e.getKey().startsWith(ENCODER_KEY, METRIC_SENDER.length() + 1)) {
				global.put(join(METRIC_V2, e.getKey().substring(METRIC_SENDER.length() + 1)), e.getValue());
			}
			else if (e.getKey().endsWith(INTERVAL_KEY)) {
				global.put(join(METRIC_ASYNC, INTERVAL_KEY), e.getValue());
			}
			else if (e.getKey().endsWith(APPEND_TYPE_KEY) && e.getValue().equals("kafka")) {
				global.put(e.getKey(), METRIC_KAFKA_SENDER_NAME);
			} else {
				global.put(e.getKey(), e.getValue());
			}
		}

		// global log level (async)
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, METRIC_SENDER));
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, METRIC_ASYNC));
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, METRIC_ENCODER));

		return global;
	}

	private static Map<String, String> extractLogPluginConfig(Map<String, String> srcCfg) {
		final String globalKey = "." + ConfigConst.PLUGIN_GLOBAL + ".";
		final String prefix = join(ConfigConst.PLUGIN, ConfigConst.OBSERVABILITY);

		String typeKey = join("", ConfigConst.PluginID.LOG, "");
		int typeKeyLength = ConfigConst.PluginID.LOG.length();

		final String reporterPrefix = LOGS;

		Map<String, String> global = extractGlobalLogConfig(srcCfg);
		HashSet<String> namespaces = new HashSet<>();
		Map<String, String> outputConfigs = new TreeMap<>(global);

		for (Map.Entry<String, String> e : srcCfg.entrySet()) {
			String key = e.getKey();
			if (!key.startsWith(prefix)) {
				continue;
			}
			int idx = key.indexOf(typeKey, prefix.length());
			if (idx < 0) {
				continue;
			}
			else {
				idx += 1;
			}
			String namespaceWithSeparator = key.substring(prefix.length(), idx);
			String suffix = key.substring(idx + typeKeyLength + 1);
			String newKey;

			if (namespaceWithSeparator.equals(globalKey)) {
				continue;
			}
			else {
				if (!namespaces.contains(namespaceWithSeparator)) {
					namespaces.add(namespaceWithSeparator);
					Map<String, String> d = ConfigUtils.extractAndConvertPrefix(global, reporterPrefix + ".", reporterPrefix + namespaceWithSeparator);
					outputConfigs.putAll(d);
				}
			}

			if (suffix.startsWith(ENCODER_KEY) || suffix.equals(ASYNC_KEY)) {
				newKey = reporterPrefix + namespaceWithSeparator + suffix;
			}
			else {
				newKey = reporterPrefix + namespaceWithSeparator + join(SENDER_KEY, suffix);
			}

			outputConfigs.put(newKey, e.getValue());
		}

		return outputConfigs;
	}

	private static Map<String, String> extractGlobalLogConfig(Map<String, String> srcCfg) {
		final String prefix = join(ConfigConst.PLUGIN, ConfigConst.OBSERVABILITY,
				ConfigConst.PLUGIN_GLOBAL, ConfigConst.PluginID.LOG);
		Map<String, String> global = new TreeMap<>();
		Map<String, String> extract = ConfigUtils.extractAndConvertPrefix(srcCfg, prefix, LOG_SENDER);

		for (Map.Entry<String, String> e : extract.entrySet()) {
			String key = e.getKey();
			if (key.startsWith(ENCODER_KEY, LOG_SENDER.length() + 1)) {
				global.put(join(LOGS, key.substring(LOG_SENDER.length() + 1)), e.getValue());
			}
			else if (key.startsWith(ASYNC_KEY, LOG_SENDER.length() + 1)) {
				global.put(join(LOGS, key.substring(LOG_SENDER.length() + 1)), e.getValue());
			}
			else {
				global.put(e.getKey(), e.getValue());
			}
		}

		// global log level (async)
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, LOG_SENDER));
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, LOG_ASYNC));
		global.putAll(ConfigUtils.extractByPrefix(srcCfg, LOG_ENCODER));

		return global;
	}
}
