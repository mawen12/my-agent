package com.mawen.agent.plugin.api.trace;


import com.mawen.agent.plugin.bridge.NoOpTracer;

/**
 * This provides utilities needed for trace instrumentation.
 *
 * <p>This type can be extended so that the object graph can be built
 * differently or overridden, for example via zipkin or when mocking
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Tracing {

	/**
	 * When true, do nothing anything and nothing is reported.
	 * However, this Tracing should still be injected into outgoing requests.
	 * Use this flag to avoid performing expensive computation.
	 *
	 * @return {@link Boolean}
	 */
	boolean isNoop();

	/**
	 * true if Thread
	 *
	 * @return {@link Boolean}
	 */
	boolean hasCurrentSpan();

	/**
	 * Returns the current span is scope or {@link NoOpTracer#NO_OP_SPAN} if there isn't one.
	 * as it is a stable type and will never return null.
	 *
	 * @return {@link Span}
	 */
	Span currentSpan();

	/**
	 * Returns a new child span if there's a {@link #currentSpan()} or a new trace if there isn't.
	 *
	 * @return {@link Span}
	 */
	Span nextSpan();

	/**
	 * get MessagingTracing for message tracing If you have a Message Server and need Span,
	 * generate result use {@link com.mawen.agent.plugin.api.Context#consumerSpan(MessagingRequest)} and
	 * {@link com.mawen.agent.plugin.api.Context#producerSpan(MessagingRequest)}.
	 *
	 * @return {@link MessagingTracing}
	 */
	MessagingTracing<MessagingRequest> messagingTracing();

	/**
	 * Returns the underlying Tracing object or {@code null} if there is none.
	 * Here is a Tracing objects: {@code brave.propagation.TraceContext}.
	 */
	Object unwrap();
}
