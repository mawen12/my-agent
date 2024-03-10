package com.mawen.agent.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.utils.ImmutableMap;
import com.mawen.agent.plugin.utils.SystemEnv;
import com.mawen.agent.plugin.utils.common.JsonUtil;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class ConfigFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigFactory.class);

	private static final String CONFIG_PROP_FILE = "agent.properties";
	private static final String CONFIG_YAML_FILE = "agent.yaml";
	public static final String AGENT_CONFIG_PATH_PROP_KEY = "agent.config.path";
	public static final String AGENT_SERVICE = "name";
	public static final String AGENT_SYSTEM = "system";
	public static final String AGENT_SERVER_PORT = "agent.server.port";
	public static final String AGENT_SERVER_ENABLED = "agent.server.enabled";
	public static final String AGENT_ENV_CONFIG = "AGENT_ENV_CONFIG";

	private static final Map<String ,String> AGENT_CONFIG_KEYS_TO_PROPS =
			ImmutableMap.<String, String>builder()
					.put("agent.name", AGENT_SERVICE)
					.put("agent.system", AGENT_SYSTEM)
					.put("agent.server.port", AGENT_SERVER_PORT)
					.put("agent.server.enabled", AGENT_SERVER_ENABLED)
					.build();

	// OTEL_SERVICE_NAME=xxx
	private static final Map<String, String> AGENT_ENV_KEY_TO_PROPS = new HashMap<>();

	static {
		for (Map.Entry<String, String> entry : AGENT_CONFIG_KEYS_TO_PROPS.entrySet()) {
			// dot.case -> UPPER_UNDERSCORE
			AGENT_ENV_KEY_TO_PROPS.put(
					ConfigPropertiesUtils.toEnvVarName(entry.getKey()),
					entry.getValue()
			);
		}
	}

	/**
	 * update config value from environment variables and java properties
	 *
	 * <p> java properties > environment variables > env:AGENT_ENV_CONFIG={} > default
	 */
	static Map<String, String> updateEnvCfg() {
		var envCfg = new TreeMap<String, String>();

		var configEnv = SystemEnv.get(AGENT_ENV_CONFIG);
		if (StringUtils.isNotEmpty(configEnv)) {
			var map = JsonUtil.toMap(configEnv);
			var strMap = new HashMap<String, String>();
			if (!map.isEmpty()) {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					strMap.put(entry.getKey(), entry.getValue().toString());
				}
			}
			envCfg.putAll(strMap);
		}

		// override by environment variables, eg: export AGENT_NAME=xxx
		for (var entry : AGENT_ENV_KEY_TO_PROPS.entrySet()) {
			var value = SystemEnv.get(entry.getKey());
			if (StringUtils.isNotEmpty(value)) {
				envCfg.put(entry.getValue(), value);
			}
		}

		// override by java properties; eg: java -Dagent-name=xxx
		for (var entry : AGENT_CONFIG_KEYS_TO_PROPS.entrySet()) {
			var value = System.getProperty(entry.getKey());
			if (StringUtils.isNotEmpty(value)) {
				envCfg.put(entry.getValue(), value);
			}
		}

		return envCfg;
	}

	private ConfigFactory() {}

	/**
	 * Get config file path from system properties or environment variables
	 */
	public static String getConfigPath() {
		// get config path from -Dagent.config.path=/agent/agent.properties || export AGENT_CONFIG_PATH=/agent/agent.properties
		var path = ConfigPropertiesUtils.getString(AGENT_CONFIG_PATH_PROP_KEY);

		if (StringUtils.isEmpty(path)) {
			// eg: -Dotel.javagent.configuration-file=/agent/agent.properties || export OTEL_JAVAAGENT_CONFIGURATION_FILE=/agent/agent.properties
			path = OtelSdkConfigs.getConfigPath();
		}
		return path;
	}

	public static GlobalConfigs loadConfigs(String pathname, ClassLoader loader) {
		// load property configuration file if exist
		var configs = loadDefaultConfigs(loader, CONFIG_PROP_FILE);

		// load yaml configuration file if exist
		var yConfigs = loadDefaultConfigs(loader, CONFIG_YAML_FILE);
		configs.mergeConfigs(yConfigs);

		// override by user special config file
		if (StringUtils.isNotEmpty(pathname)) {
			var configFromOuterFile = ConfigLoader.loadFromFile(new File(pathname));
			LOGGER.info("Loaded user special config file: {}", pathname);
			configs.mergeConfigs(configFromOuterFile);
		}

		// override by opentelemetry sdk env config
		configs.updateConfigsNotNotify(OtelSdkConfigs.updateEnvCfg());

		// check environment cfg override
		configs.updateConfigsNotNotify(updateEnvCfg());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Loaded conf:\n{}", configs.toPrettyDisplay());
		}
		return configs;
	}

	private static GlobalConfigs loadDefaultConfigs(ClassLoader loader, String file) {
		var globalConfigs = JarFileConfigLoader.load(file);
		return globalConfigs != null ? globalConfigs : ConfigLoader.loadFromClasspath(loader, file);
	}
}
