package com.mawen.agent.httpserver.nanohttpd.util;

/**
 * Represents a simple factory
 *
 * @param <T> The type of object to create
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IFactory<T> {

	T create();
}
