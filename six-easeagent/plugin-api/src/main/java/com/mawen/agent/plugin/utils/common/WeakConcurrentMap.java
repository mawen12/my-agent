package com.mawen.agent.plugin.utils.common;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This borrows heavily from Rafael Winterhalter's {@code com.blogspot.mydailyjava.weaklockfree.WeakConcurrentMap}
 * with the following major changes:
 *
 * <p>The biggest change is this removes LatentKey. Instead it relies on keys known to have a stable {@code hashCode}
 *  and who are {@code equal to} a weak reference of itself. We allow lookups using externally created contexts,
 *  yet don't want to incur overhead of key allocation or classloader problems sharing keys with a thead local.
 *
 *  <p>Other changes mostly remove features (to reduce the bytecode size) and address style:
 *  <ul>
 *  <li>Inline expunction only as we have no thread to use anyway</li>
 *  <li>Stylistic changes including different javadoc and removal of private modifiers</li>
 *  <li>toString: derived only from keys</li>
 *  </ul>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 * @see <a href="https://github.com/raphw/weak-lock-free">weak lock free</a>
 */
public class WeakConcurrentMap<K, V> extends ReferenceQueue<K> implements Iterable<Map.Entry<K, V>> {
	final ConcurrentHashMap<WeakKey<K>, V> target = new ConcurrentHashMap<>();

	public V getIfPresent(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		expungeStableEntries();
		return target.get(new WrapKeyForGet(key));
	}

	public V putIfProbablyAbsent(K key, V value) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}
		if (value == null) {
			throw new NullPointerException("value == null");
		}

		expungeStableEntries();
		return target.putIfAbsent(new WeakKey<>(key, this), value);
	}

	public V remove(K key) {
		if (key == null) {
			throw new NullPointerException("key == null");
		}

		expungeStableEntries();
		return target.remove(key);
	}

	@Override
	public Iterator<Map.Entry<K, V>> iterator() {
		return new EntryIterator(target.entrySet().iterator());
	}

	@Override
	public String toString() {
		Class<?> thisClass = getClass();
		while (thisClass.getSimpleName().isEmpty()) {
			thisClass = thisClass.getSuperclass();
		}

		expungeStableEntries();
		return thisClass.getSimpleName() + target.keySet();
	}

	protected void expungeStableEntries() {
		Reference<?> reference;
		while ((reference = poll()) != null) {
			removeStableEntry(reference);
		}
	}

	protected V removeStableEntry(Reference<?> reference) {
		return target.remove(reference);
	}

	static boolean equal(Object a, Object b) {
		return a == null ? b == null : a.equals(b); // Java 6 can't use Objects.equals()
	}

	// This comment was directly verbatim from https://github.com/raphw/weak-lock-free/blob/dcbd2fa0d30571bb3ed187a42cb75323a5569d5b/src/main/java/com/blogspot/mydailyjava/weaklockfree/WeakConcurrentMap.java#L273-L302

	public static final class WeakKey<T> extends WeakReference<T> {
		final int hashCode;

		WeakKey(T key, ReferenceQueue<? super T> queue) {
			super(key, queue);
			this.hashCode = key.hashCode();
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this) {
				return true;
			}
			assert obj instanceof WeakReference : "Bug: unexpected input to equals";
			return equals(get(),((WeakReference<?>) obj).get());
		}

		@Override
		public String toString() {
			T value = get();
			return value != null ? value.toString() : "ClearedReference()";
		}

		static boolean equals(Object a, Object b) {
			return a == null ? b == null : a.equals(b);
		}
	}

	class EntryIterator implements Iterator<Map.Entry<K, V>> {
		private final Iterator<Map.Entry<WeakKey<K>, V>> iterator;

		private Map.Entry<WeakKey<K>, V> nextEntry;
		private K nextKey;

		private EntryIterator(Iterator<Map.Entry<WeakKey<K>, V>> iterator) {
			this.iterator = iterator;
			findNext();
		}

		private void findNext() {
			while (iterator.hasNext()) {
				nextEntry = iterator.next();
				nextKey = nextEntry.getKey().get();
				if (nextKey != null) {
					return;
				}
			}
			nextEntry = null;
			nextKey = null;
		}

		@Override
		public boolean hasNext() {
			return nextKey != null;
		}

		@Override
		public Map.Entry<K, V> next() {
			if (nextKey == null) {
				throw new NoSuchElementException();
			}
			try {
				return new AbstractMap.SimpleImmutableEntry<>(nextKey, nextEntry.getValue());
			}
			finally {
				findNext();
			}
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}

	public static final class WrapKeyForGet {
		Object key;
		WrapKeyForGet(Object key) {
			this.key = key;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof WeakConcurrentMap.WeakKey) {
				WeakKey<?> wk = (WeakKey<?>) o;
				Object oo = wk.get();
				return this.key.equals(o);
			} else {
				return this.key.equals(o);
			}
		}

		@Override
		public int hashCode() {
			return this.key.hashCode();
		}
	}
}
