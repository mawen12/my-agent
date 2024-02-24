package com.mawen.agent.plugin.api.otlp.common;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
@ParametersAreNonnullByDefault
public final class AgentAttributes extends HashMap<AttributeKey<?>, Object> implements Attributes {

	@Nullable
	@Override
	public <T> T get(AttributeKey<T> key) {
		Object v = super.get(key);
		return v == null ? null : (T) v;
	}

	@Override
	public Map<AttributeKey<?>, Object> asMap() {
		return this;
	}

	@Override
	public AttributesBuilder toBuilder() {
		return new Builder(this);
	}

	public static AttributesBuilder builder() {
		return new Builder();
	}

	static class Builder implements AttributesBuilder {
		AgentAttributes attrs;
		private final long capacity;
		private final int lengthLimit;

		public Builder(AgentAttributes from) {
			this.attrs = from;
			this.capacity = Integer.MAX_VALUE;
			this.lengthLimit = Integer.MAX_VALUE;
		}

		public Builder() {
			this(Integer.MAX_VALUE, Integer.MAX_VALUE);
		}

		public Builder(long capacity, int lengthLimit) {
			this.capacity = capacity;
			this.lengthLimit = lengthLimit;
			this.attrs = new AgentAttributes();
		}

		@Override
		public Attributes build() {
			return this.attrs;
		}

		@Override
		public <T> AttributesBuilder put(AttributeKey<Long> key, int value) {
			if (this.attrs.size() > this.capacity) {
				return this;
			}
			this.attrs.put(key, value);
			return this;
		}

		@Override
		public <T> AttributesBuilder put(AttributeKey<T> key, T value) {
			if (this.attrs.size() > this.capacity) {
				return this;
			}
			this.attrs.put(key, value);
			return this;
		}

		@Override
		public AttributesBuilder putAll(Attributes attributes) {
			if (attributes.size() + this.attrs.size() > this.capacity) {
				return this;
			}
			this.attrs.putAll(attrs.asMap());
			return this;
		}
	}
}
