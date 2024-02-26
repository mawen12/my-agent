package com.mawen.agent.config;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Splitter;
import com.mawen.agent.plugin.utils.ImmutableMap;
import com.mawen.agent.plugin.utils.SystemEnv;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * Compatible with opentelemetry-java.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 * @see <a href="https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#disabling-opentelemetrysdk">Disabling opentelemetrysdk</a>
 */
public class OtelSdkConfigs {

	private static final String OTEL_RESOURCE_ATTRIBUTES_KEY = "otel.resource.attributes";
	private static final String CONFIG_PATH_PROP_KEY = "otel.javaagent.configuration-file";
	private static final Splitter.MapSplitter OTEL_RESOURCE_ATTRIBUTES_SPLITTER =
			Splitter.on(",")
					.omitEmptyStrings()
					.withKeyValueSeparator("=");
	private static final Map<String, String> SDK_ATTRIBUTES_TO_AGENT_PROPS =
			ImmutableMap.<String, String>builder()
					.put("sdk.disabled", "agent.server.enabled")
					.put("service.name", "name") // "agent.name"
					.put("service.namespace", "system") // "agent.system"
					.build();

	// -Dotel.service.name=xxx
	private static final Map<String, String> OTEL_SDK_PROPS_TO_AGENT_PROPS = new HashMap<>();

	// OTEL_SERVICE_NAME=xxx
	private static final Map<String, String> OTEL_SDK_ENV_VAR_TO_AGENT_PROPS = new HashMap<>();

	static {
		for (Map.Entry<String, String> entry : SDK_ATTRIBUTES_TO_AGENT_PROPS.entrySet()) {
			// lower.hyphen -> UPPER_UNDERSCORE
			OTEL_SDK_PROPS_TO_AGENT_PROPS.put(
					"otel." + entry.getKey(),
					entry.getValue()
			);
		}

		for (Map.Entry<String, String> entry : OTEL_SDK_PROPS_TO_AGENT_PROPS.entrySet()) {
			// dot.case -> UPPER_UNDERSCORE
			OTEL_SDK_ENV_VAR_TO_AGENT_PROPS.put(
					ConfigPropertiesUtils.toEnvVarName(entry.getKey()),
					entry.getValue()
			);
		}
	}

	/**
	 * Get config path from java properties or environment variables
	 */
	static String getConfigPath() {
		return ConfigPropertiesUtils.getString(CONFIG_PATH_PROP_KEY);
	}

	/**
	 * update config value from environment variables and java properties
	 *
	 * <p>java properties > environment variables > OTEL_RESOURCE_ATTRIBUTES
	 */
	static Map<String, String> updateEnvCfg() {
		Map<String, String> envCfg = new TreeMap<>();

		String configEnv = ConfigPropertiesUtils.getString(OTEL_RESOURCE_ATTRIBUTES_KEY);
		if (StringUtils.isNotEmpty(configEnv)) {
			Map<String, String> map = OTEL_RESOURCE_ATTRIBUTES_SPLITTER.split(configEnv);
			if (!map.isEmpty()) {
				for (Map.Entry<String, String> entry : SDK_ATTRIBUTES_TO_AGENT_PROPS.entrySet()) {
					String value = map.get(entry.getKey());
					if (!StringUtils.isEmpty(value)) {
						envCfg.put(entry.getValue(), value);
					}
				}
			}
		}

		// override by environment variables, eg: export OTEL_SERVICE_NAME=xxx
		for (Map.Entry<String, String> entry : OTEL_SDK_ENV_VAR_TO_AGENT_PROPS.entrySet()) {
			String value = SystemEnv.get(entry.getKey());
			if (!StringUtils.isEmpty(value)) {
				envCfg.put(entry.getValue(), value);
			}
		}

		// override by java properties; eg: java -Dotel.service.name=xxx
		for (Map.Entry<String, String> entry : OTEL_SDK_PROPS_TO_AGENT_PROPS.entrySet()) {
			String value = System.getProperty(entry.getKey());
			if (!StringUtils.isEmpty(value)) {
				envCfg.put(entry.getValue(), value);
			}
		}

		return envCfg;
	}
}
