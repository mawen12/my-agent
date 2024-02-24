package com.mawen.agent.plugin.report;

import java.io.IOException;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Call<V> {

	V execute() throws IOException;

	default void enqueue(Callback<V> cb) {

	}
}
