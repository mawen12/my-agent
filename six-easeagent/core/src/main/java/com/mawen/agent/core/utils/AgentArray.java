package com.mawen.agent.core.utils;


import java.util.Arrays;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import com.google.auto.service.AutoService;
import com.mawen.agent.core.AppendBootstrapClassLoaderSearch;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@AutoService(AppendBootstrapClassLoaderSearch.class)
public class AgentArray<E> {

	private static final int DEFAULT_INIT_SIZE = 256;
	private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
	private final Object lock = new Object();
	private Object[] e;
	private final AtomicInteger size = new AtomicInteger(0);

	public AgentArray() {
		this(DEFAULT_INIT_SIZE);
	}

	public AgentArray(int initSize) {
		this.e = new Object[initSize];
		e[0] = lock;
		size.incrementAndGet();
	}

	public int size() {
		return size.get();
	}

	public Object[] toArray() {
		return e.clone();
	}

	public int add(E element) {
		int current = size.get();
		while (!size.compareAndSet(current, current + 1)) {
			current = size.get();
		}
		ensureCapacity(current + 1);
		e[current] = element;

		return current;
	}

	public E get(int index) {
		if (index >= size()) {
			return null;
		}
		return (E) e[index];
	}

	public E getUncheck(int index) {
		return (E) e[index];
	}

	public E putIfAbsent(int index, E element) {
		ensureCapacity(index + 1);
		E oldValue;

		synchronized (lock) {
			oldValue = (E) e[index];
			if (oldValue == null) {
				e[index] = element;
			} else {
				return oldValue;
			}
		}

		int currentSize = size.get();
		if (currentSize < index + 1) {
			while (!size.compareAndSet(currentSize, index + 1)) {
				currentSize = size.get();
				if (currentSize > index + 1) {
					break;
				}
			}
		}
		return null;
	}

	public E replace(int index, E element) {
		ensureCapacity(index + 1);
		E oldValue;

		synchronized (lock) {
			oldValue = (E) e[index];
			if (oldValue == null) {
				return null;
			}
			else {
				e[index] = element;
			}
		}
		return oldValue;
	}

	public int indexOf(Object o) {
		int length = size();
		Object s;
		if (o == null) {
			for (int i = 0; i < length; i++) {
				s = e[i];
				if (s == null) {
					return i;
				}
			}
		} else {
			for (int i = 0; i < length; i++) {
				s = e[i];
				if (o.equals(s)) {
					return i;
				}
			}
		}
		return -1;
	}

	public boolean contains(Object o) {
		return indexOf(o) != -1;
	}

	public Spliterator<E> spliterator() {
		return Spliterators.spliterator(e, Spliterator.ORDERED);
	}

	public void forEach(Consumer<? super E> action) {
		Objects.requireNonNull(action);
		E element;
		for (int i = 0; i < size.get(); i++) {
			element = (E) e[i];
			action.accept(element);
		}
	}

	private void ensureCapacity(int minCapacity) {
		if (minCapacity - e.length > 0) {
			grow(minCapacity);
		}
	}

	private synchronized void grow(int minCapacity) {
		int oldCapacity = e.length;
		int newCapacity = oldCapacity + (oldCapacity >> 1);
		if (newCapacity - minCapacity < 0) {
			newCapacity = minCapacity;
		}
		if (newCapacity - MAX_ARRAY_SIZE > 0) {
			newCapacity = hugeCapacity(minCapacity);
		}
		this.e = Arrays.copyOf(e, newCapacity);
	}

	private static int hugeCapacity(int minCapacity) {
		if (minCapacity < 0) {
			throw new OutOfMemoryError();
		}
		return (minCapacity > MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
	}

	private String outOfBoundsMsg(int index) {
		return "Index: " + index + ", Size: " + size;
	}
}
