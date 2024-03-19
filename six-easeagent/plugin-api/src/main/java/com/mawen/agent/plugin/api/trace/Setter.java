package com.mawen.agent.plugin.api.trace;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Setter {

	Setter NOOP_INSTANCE = (name, value) -> {};

	/**
	 * @param name header name
	 * @param value header value
	 */
	void setHeader(String name, String value);
}
