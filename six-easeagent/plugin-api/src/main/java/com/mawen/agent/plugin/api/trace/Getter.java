package com.mawen.agent.plugin.api.trace;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Getter {

	/**
	 * @param name header name
	 * @return header value
	 */
	String header(String name);
}
