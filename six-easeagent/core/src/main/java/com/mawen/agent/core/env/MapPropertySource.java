package com.mawen.agent.core.env;

import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class MapPropertySource extends EnumerablePropertySource<Map<String, Object>> {

	public MapPropertySource(String name, Map<String, Object> source) {
		super(name, source);
	}

	@Override
	public Object getProperty(String name) {
		return this.source.get(name);
	}

	@Override
	public boolean containsProperty(String name) {
		return this.source.containsKey(name);
	}

	@Override
	public String[] getPropertyNames() {
		Set<String> keys = this.source.keySet();
		return CollectionUtils.isNotEmpty(keys) ? keys.toArray(new String[0]) : new String[]{};
	}
}
