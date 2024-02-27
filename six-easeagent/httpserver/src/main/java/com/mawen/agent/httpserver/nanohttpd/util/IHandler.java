package com.mawen.agent.httpserver.nanohttpd.util;

/**
 * Defines a generic handler that returns an object of type O
 * when given an object of type I.
 *
 * @param <I> The input type.
 * @param <O> The output type.
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface IHandler<I, O> {

	O handle(I input);
}
