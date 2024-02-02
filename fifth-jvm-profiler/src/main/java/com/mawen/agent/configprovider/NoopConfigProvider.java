package com.mawen.agent.configprovider;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ListModel;

import com.mawen.agent.ConfigProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class NoopConfigProvider implements ConfigProvider {

	@Override
	public Map<String, Map<String, List<String>>> getConfig() {
		Map<String, Map<String, List<String>>> configMap = new HashMap<>();

		Map<String, List<String>> values = new HashMap<>();
		values.put("noop", Arrays.asList("true"));

		configMap.put("", values);
		return configMap;
	}
}
