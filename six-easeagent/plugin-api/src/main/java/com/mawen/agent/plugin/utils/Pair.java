package com.mawen.agent.plugin.utils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class Pair<K, V> {
	private final K key;
	private final V value;

	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}
}
