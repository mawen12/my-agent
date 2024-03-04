package com.mawen.agent.mock.context;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class RetBound {

	@Getter
	int size;

	Map<String, Object> local;

	public RetBound(int size) {
		this.size = size;
	}

	public Object get(String key) {
		if (local == null) {
			return null;
		}
		return local.get(key);
	}

	public void put(String key, Object value) {
		if (local == null) {
			this.local = new HashMap<>();
		}
		this.local.put(key, value);
	}
}
