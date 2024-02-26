package com.mawen.agent.plugin.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ImmutableMap<K, V> implements Map<K, V> {
	Map<K, V> delegate;

	public ImmutableMap(Map<K, V> map) {
		this.delegate = map;
	}

	@Override
	public int size() {
		return delegate.size();
	}

	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return delegate.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return delegate.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return delegate.get(key);
	}

	@Override
	public V put(K key, V value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public V remove(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<K> keySet() {
		return delegate.keySet();
	}

	@Override
	public Collection<V> values() {
		return delegate.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return delegate.entrySet();
	}

	public static <K, V> Builder<K, V> builder() {
		return new Builder<>();
	}

	public static class Builder<K, V> {
		Map<K, V> result = new HashMap<>();

		public Builder<K, V> put(K key, V value) {
			result.put(key, value);
			return this;
		}

		public Map<K, V> build() {
			return new ImmutableMap<>(result);
		}
	}

}
