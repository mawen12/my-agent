package com.mawen.agent.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

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
		String agentJarPath = System.getProperty(ConfigConst.AGENT_JAR_PATH);
		if (agentJarPath == null) {
			return null;
		}
		try {
			JarFile jarFile = new JarFile(new File(agentJarPath));
			ZipEntry zipEntry = jarFile.getEntry(file);
			if (zipEntry == null) {
				return null;
			}
			try (InputStream in = jarFile.getInputStream(zipEntry)) {
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
