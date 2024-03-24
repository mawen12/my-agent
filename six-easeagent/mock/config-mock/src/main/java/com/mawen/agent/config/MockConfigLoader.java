package com.mawen.agent.config;

import java.io.File;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class MockConfigLoader {
	public static Configs loadFromFile(File file) {
		return ConfigLoader.loadFromFile(file);
	}
}
