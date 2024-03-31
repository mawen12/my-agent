package com.mawen.agent.core.env;

import java.util.Objects;

import jdk.jfr.internal.StringPool;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public abstract class EnumerablePropertySource<T> extends PropertySource<T> {

	public EnumerablePropertySource(String name, T source) {
		super(name, source);
	}

	@Override
	public boolean containsProperty(String name) {
		return ArrayUtils.contains(getPropertyNames(),name);
	}

	public abstract String[] getPropertyNames();
}
