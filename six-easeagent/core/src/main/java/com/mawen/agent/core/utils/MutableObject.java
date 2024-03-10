package com.mawen.agent.core.utils;

import lombok.Data;

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

	@Data
	class DefaultMutableObject<T> implements MutableObject<T> {
		private T value;

		protected DefaultMutableObject(T value) {
			this.value = value;
		}

		@Override
		public String toString() {
			return value == null ? "null" : value.toString();
		}
	}
}
