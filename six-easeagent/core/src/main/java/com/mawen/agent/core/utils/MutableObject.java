package com.mawen.agent.core.utils;

import java.util.Objects;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public interface MutableObject<T> {

	static <S> MutableObject<S> wrap(S object) {
		return new DefaultMutableObject<>(object);
	}

	static <S> MutableObject<S> nullMutableObject() {
		return new DefaultMutableObject<>(null);
	}

	T getValue();

	void setValue(T value);

	class DefaultMutableObject<T> implements MutableObject<T> {
		private T value;

		protected DefaultMutableObject(T value) {
			this.value = value;
		}

		@Override
		public T getValue() {
			return value;
		}

		@Override
		public void setValue(T value) {
			this.value = value;
		}

		@Override
		public final boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof DefaultMutableObject<?> that)) return false;

			return Objects.equals(value, that.value);
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}

		@Override
		public String toString() {
			return value == null ? "null" : value.toString();
		}
	}
}
