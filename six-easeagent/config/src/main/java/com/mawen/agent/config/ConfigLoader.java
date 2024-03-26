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

	static Configs loadFromClasspath(ClassLoader classLoader, String file) {
		try (InputStream in = classLoader.getResourceAsStream(file)) {
			return ConfigLoader.loadFromStream(in, file);
		}
		catch (IOException e) {
			log.warn("Load config file: {} by classloader: {} failure: {}", file, classLoader.toString(), e);
		}
		return new Configs(Collections.emptyMap());
	}

	static Configs loadFromStream(InputStream in, String filename) throws IOException {
		Map<String, String> map = Collections.emptyMap();
		if (in != null) {
			if (checkYaml(filename)) {
				try {
					map = new YamlReader().load(in).compress();
				}
				catch (Exception e) {
					log.warn("Wrong Yaml format, load config file failure: {}", filename);
				}
			} else {
				map = extractPropsMap(in);
			}
		}
		return new Configs(map);
	}

	static Configs loadFromFile(File file) {
		try (InputStream in = new FileInputStream(file)) {
			return ConfigLoader.loadFromStream(in, file.getAbsolutePath());
		}
		catch (IOException e) {
			log.warn("Load config file failure: {}", file.getAbsolutePath());
		}
		return new Configs(Collections.emptyMap());
	}

	private static boolean checkYaml(String filename) {
		return filename.endsWith(".yaml") || filename.endsWith(".yml");
	}

	private static Map<String, String> extractPropsMap(InputStream in) throws IOException {
		Properties properties = new Properties();
		properties.load(in);

		return properties.stringPropertyNames()
				.stream()
				.collect(Collectors.toMap(Function.identity(), name -> String.valueOf(properties.getProperty(name))));
	}
}
