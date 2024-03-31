package com.mawen.agent.core.env;

import java.util.Objects;

import com.mawen.agent.core.utils.Assert;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * Copied from Spring's source.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public abstract class PropertySource<T> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	protected final String name;

	protected final T source;

	public PropertySource(String name, T source) {
		Assert.hasText(name,"Property source name must contains at least one character");
		Assert.notNull(source,"Property source must not be null");
		this.name = name;
		this.source = source;
	}

	public String getName() {
		return this.name;
	}

	public T getSource() {
		return this.source;
	}

	public boolean containsProperty(String name) {
		return getProperty(name) != null;
	}

	public abstract Object getProperty(String name);

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof PropertySource<?>)) {
			return false;
		}

		PropertySource<?> other = (PropertySource<?>) obj;
		return Objects.equals(getName(), other.getName());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getName());
	}

	@Override
	public String toString() {
		if (logger.isDebugEnabled()) {
			return getClass().getSimpleName() + "@" + System.identityHashCode(this) +
					" {name='" + getName() + "', properties=" + getSource() + "}";
		} else {
			return getClass().getSimpleName() + " {name='" + getName() + "'}";
		}
	}
}
