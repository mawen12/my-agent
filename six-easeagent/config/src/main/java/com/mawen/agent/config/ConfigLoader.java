package com.mawen.agent.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.mawen.agent.config.yaml.YamlReader;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class ConfigLoader {
	private static final Logger log = LoggerFactory.getLogger(ConfigLoader.class);

	private static boolean checkYaml(String filename) {
		return filename.endsWith(".yaml") || filename.endsWith(".yml");
	}

	static GlobalConfigs loadFromClasspath(ClassLoader classLoader, String file) {
		try (var in = classLoader.getResourceAsStream(file)) {
			return ConfigLoader.loadFromStream(in, file);
		}
		catch (IOException e) {
			log.warn("Load config file: {} by classloader: {} failure: {}", file, classLoader, e);
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
					log.warn("Wrong Yaml format, load config file failure: {}", filename);
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

	private static Map<String, String> extractPropsMap(InputStream in) throws IOException {
		var properties = new Properties();
		properties.load(in);

		return properties.stringPropertyNames()
				.stream()
				.collect(Collectors.toMap(Function.identity(), name -> String.valueOf(properties.getProperty(name))));
	}

	static GlobalConfigs loadFromFile(File file) {
		try (var in = new FileInputStream(file)) {
			return ConfigLoader.loadFromStream(in, file.getAbsolutePath());
		}
		catch (IOException e) {
			log.warn("Load config file failure: {}", file.getAbsolutePath());
		}
		return new GlobalConfigs(Collections.emptyMap());
	}
}
