package com.mawen.agent.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.mawen.agent.config.yaml.YamlReader;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class ConfigLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigLoader.class);

	private static boolean checkYaml(String filename) {
		return filename.endsWith(".yaml") || filename.endsWith(".yml");
	}

	static GlobalConfigs loadFromFile(File file) {
		try (FileInputStream in = new FileInputStream(file)) {
			return ConfigLoader.loadFromStream(in, file.getAbsolutePath());
		}
		catch (IOException e) {
			LOGGER.warn("Load config file failure: {}", file.getAbsolutePath());
		}
		return new GlobalConfigs(Collections.emptyMap());
	}

	static GlobalConfigs loadFromStream(InputStream in, String filename) throws IOException {
		if (in != null) {
			Map<String, String> map;
			if (checkYaml(filename)) {
				try {
					map = new YamlReader().load(in).compress();
				}
				catch (Exception e) {
					LOGGER.warn("Wrong Yaml format, load config file failure: {}", filename);
					map = Collections.emptyMap();
				}
			} else {
				map = extractPropsMap(in);
			}
			return new GlobalConfigs(map);
		} else {
			return new GlobalConfigs(Collections.emptyMap());
		}
	}

	private static HashMap<String, String> extractPropsMap(InputStream in) throws IOException {
		Properties properties = new Properties();
		properties.load(in);
		HashMap<String, String> map = new HashMap<>();
		for (String one : properties.stringPropertyNames()) {
			map.put(one, properties.getProperty(one));
		}
		return map;
	}

	static GlobalConfigs loadFromClasspath(ClassLoader classLoader, String file) {
		try (InputStream in = classLoader.getResourceAsStream(file)) {
			return ConfigLoader.loadFromStream(in, file);
		}
		catch (IOException e) {
			LOGGER.warn("Load config file: {} by classloader: {} failure: {}", file, classLoader.toString(), e);
		}
		return new GlobalConfigs(Collections.emptyMap());
	}
}
