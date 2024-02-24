package com.mawen.agent.plugin.api.trace;

import com.mawen.agent.plugin.bridge.NoOpTracer.EmptyExtractor;

/**
 * Used to continue an incoming trace. For example, by reading http headers.
 *
 * <p>Note: This type if safe to implement as a lambda,
 * or use as a method reference as it is effectively a {@link FunctionalInterface}.
 * It isn't annotated as such because the project has a minimum Java language level 6.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 * @see Tracing#nextSpan(Message)
 */
public interface Extractor<R extends MessagingRequest> {

	/**
	 * Returns either a trace context or sampling flags parsed from the request.
	 * If nothing was parsable, sampling flags will be set to {@link com.mawen.agent.plugin.bridge.NoOpTracer},
	 * {@link EmptyExtractor#INSTANCE}.
	 *
	 * @param request holds propagation fields. For example, an incoming message or http request.
	 */
	Message extract(R request);
}
