package com.mawen.agent.core.plugin.matcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public interface Converter<S, T> {

	T convert(S source);
}
