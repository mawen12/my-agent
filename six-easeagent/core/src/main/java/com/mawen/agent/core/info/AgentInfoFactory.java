package com.mawen.agent.core.info;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.bridge.AgentInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class AgentInfoFactory {
	private static final Logger logger = LoggerFactory.getLogger(AgentInfoFactory.class);
	private static final String AGENT_TYPE = "Agent";
	private static final String VERSION_FILE = "version.txt";

	public static AgentInfo loadAgentInfo(ClassLoader loader) {
		return new AgentInfo(AGENT_TYPE, loadVersion(loader, VERSION_FILE));
	}

	/**
	 * load from core src/main/resource version.txt
	 *
	 * @return return version in file
	 */
	private static String loadVersion(ClassLoader loader, String file) {
		try (InputStream in = loader.getResourceAsStream(file)) {
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String version = reader.readLine();
			reader.close();
			return version;
		}
		catch (IOException e) {
			logger.warn("Load config file:{} by classloader:{} failure: {}",file, loader.toString(), e);
		}
		return "";
	}

}
