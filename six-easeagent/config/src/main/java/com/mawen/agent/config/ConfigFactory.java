package com.mawen.agent.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.utils.ImmutableMap;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class ConfigFactory {
	private static final Logger log = LoggerFactory.getLogger(ConfigFactory.class);

	private static final String CONFIG_PROP_FILE = "agent.properties";
	private static final String CONFIG_YAML_FILE = "agent.yaml";
	public static final String AGENT_CONFIG_PATH_PROP_KEY = "agent.config.path";
	public static final String AGENT_SERVICE = "name";
	public static final String AGENT_SYSTEM = "system";
	public static final String AGENT_SERVER_PORT = "agent.server.port";
	public static final String AGENT_SERVER_ENABLED = "agent.server.enabled";

	/**
	 * Get config file path from system properties or environment variables
	 *
	 * <p>Agent Config:
	 * <pre>
	 * 	1.{@code -Dagent.config.path=/agent/agent.properties}
	 * 	2.{@code export AGENT_CONFIG_PATH=/agent/agent.properties}
	 * </pre>
	 */
	public static String getConfigPath() {
		// get config path from -Dagent.config.path=/agent/agent.properties || export AGENT_CONFIG_PATH=/agent/agent.properties
		return ConfigPropertiesUtils.getString(AGENT_CONFIG_PATH_PROP_KEY);
	}

	public static Configs loadConfigs(String pathname, ClassLoader loader) {
		// load property configuration file if exist
		Configs configs = loadDefaultConfigs(loader, CONFIG_PROP_FILE);

		// load yaml configuration file if exist
		var yConfigs = loadDefaultConfigs(loader, CONFIG_YAML_FILE);
		configs.mergeConfigs(yConfigs);

		// override by user special config file
		if (StringUtils.isNotEmpty(pathname)) {
			var configFromOuterFile = ConfigLoader.loadFromFile(new File(pathname));
			log.info("Loaded user special config file: {}", pathname);
			configs.mergeConfigs(configFromOuterFile);
		}

		log.debugIfEnabled("Loaded conf:\n{}", configs.toString());
		return configs;
	}

	private static Configs loadDefaultConfigs(ClassLoader loader, String file) {
		var globalConfigs = JarFileConfigLoader.load(file);
		return NoNull.of(globalConfigs, ConfigLoader.loadFromClasspath(loader, file));
	}

	private ConfigFactory() {
	}
}
