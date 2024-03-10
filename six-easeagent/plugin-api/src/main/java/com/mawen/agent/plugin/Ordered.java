package com.mawen.agent.plugin;

/**
 * Priority definition interface.
 * Higher values operate later.
 * For example: an interceptor with order=1 will be called after an interceptor with order=0
 *
 * @see com.mawen.agent.plugin.enums.Order
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Ordered {

	int order();
}
