package com.mawen.agent.httpserver.nanohttpd.util;

/**
 * Represents a factory that can throw an exception instead of actually creating an object.
 *
 * @param <T> The type of object to create
 * @param <E> The base Type of exceptions that can be thrown
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IFactoryThrowing<T, E extends Throwable> {
	T create() throws E;
}
