package com.mawen.agent.config;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.ConfigConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class JarFileConfigLoader {
	private static final Logger log = LoggerFactory.getLogger(JarFileConfigLoader.class);

	static Configs load(String file) {
		var agentJarPath = System.getProperty(ConfigConst.AGENT_JAR_PATH);
		if (agentJarPath == null) {
			return null;
		}
		try {
			var jarFile = new JarFile(new File(agentJarPath));
			var zipEntry = jarFile.getEntry(file);
			if (zipEntry == null) {
				return null;
			}
			try (var in = jarFile.getInputStream(zipEntry)) {
				return ConfigLoader.loadFromStream(in, file);
			}
			catch (IOException e) {
				log.warn("Load config file:{} failure: {}", file, e);
			}
		}
		catch (IOException e) {
			log.warn("create JarFile:{} failure:{}", agentJarPath, e);
		}

		return null;
	}
}
