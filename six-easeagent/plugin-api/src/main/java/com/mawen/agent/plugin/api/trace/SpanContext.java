package com.mawen.agent.plugin.api.trace;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface SpanContext {

	/**
	 * When true, do nothing anything and nothing is reported.
	 * However, this Tracing should still be injected into outgoing requests.
	 * Use this flag to avoid performing expensive computation.
	 */
	boolean isNoop();

	/**
	 * Returns the underlying Span Context object or {@code null} if there is none.
	 * Here is a span Context objects: {@code brave.propagation.TraceContext}
	 */
	Object unwrap();
}
