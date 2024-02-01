package com.mawen.agent.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.ConfigProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class DummyConfigProvider implements ConfigProvider {

	@Override
	public Map<String, Map<String, List<String>>> getConfig() {
		return new HashMap<>();
	}
}
