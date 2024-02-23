package com.mawen.agent.plugin.api.logging;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Mdc {

	void put(String key, String value);

	void remove(String key);

	String get(String key);

}
