package com.mawen.agent.plugin.api.trace;

import com.mawen.agent.plugin.api.Context;

/**
 * Subtype of {@link Context} which can set up Tracing.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface TracingContext extends Context {

	/**
	 * set up tracing to the session tracing context
	 *
	 * @param tracing {@link ITracing}
	 */
	void setCurrentTracing(ITracing tracing);
}
