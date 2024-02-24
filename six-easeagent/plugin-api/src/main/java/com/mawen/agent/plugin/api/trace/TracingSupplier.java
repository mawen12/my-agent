package com.mawen.agent.plugin.api.trace;

import java.util.function.Supplier;

import com.mawen.agent.plugin.api.InitializeContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface TracingSupplier {
	ITracing get(Supplier<InitializeContext> contextSupplier);
}
