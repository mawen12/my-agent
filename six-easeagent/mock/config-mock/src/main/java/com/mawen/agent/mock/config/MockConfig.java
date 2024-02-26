package com.mawen.agent.mock.config;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.config.Configs;
import com.mawen.agent.config.GlobalConfigs;
import com.mawen.agent.config.MockConfigLoader;
import com.mawen.agent.config.PluginConfigManager;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class MockConfig {
	private static final String MOCK_CONFIG_YAML_FILE = "mock_agent.yaml";
	private static final String MOCK_CONFIG_PROP_FILE = "mock_agent.properties";
	private static final GlobalConfigs CONFIGS;
	private static final PluginConfigManager PLUGIN_CONFIG_MANAGER;

	static {
		Map<String, String> initConfigs = new HashMap<>();
		initConfigs.put("name", "demo-service");
		initConfigs.put("system", "demo-system");

		initConfigs.put("observability.outputServer.timeout", "10000");
		initConfigs.put("observability.outputServer.enabled", "true");
		initConfigs.put("observability.tracings.output.enabled", "true");
		initConfigs.put("plugin.observability.global.tracing.enabled", "true");
		initConfigs.put("plugin.observability.global.metric.enabled", "true");
		CONFIGS = new GlobalConfigs(initConfigs);
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		URL url = classLoader.getResource(MOCK_CONFIG_YAML_FILE);
		if (url == null) {
			url = classLoader.getResource(MOCK_CONFIG_PROP_FILE);
		}
		if (url != null) {
			GlobalConfigs configsFromOuterFile = MockConfigLoader.loadFromFile(new File(url.getFile()));
			CONFIGS.mergeConfigs(configsFromOuterFile);
		}
		PLUGIN_CONFIG_MANAGER = PluginConfigManager.builder(CONFIGS).build();
	}

	public static Configs getConfigs() {
		return CONFIGS;
	}

	public static PluginConfigManager getPluginConfigManager() {
		return PLUGIN_CONFIG_MANAGER;
	}
}
