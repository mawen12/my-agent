package com.mawen.agent.plugin.report.tracing;

import java.util.List;
import java.util.Map;

/**
 * borrow from zipkin2.Span
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface ReportSpan {

	/**
	 * span base
	 */
	String traceId();

	/**
	 * span parent id
	 */
	String parentId();

	/**
	 * spanId
	 */
	String id();

	/**
	 * Span.Kind.name
	 */
	String kind();

	/**
	 * Span name in lowercase, rpc method for example.
	 * Conventionally, when the span name isn't known, name = "unknown".
	 */
	String name();

	/**
	 * Epoch microseconds of the start of this span,
	 * possibly zero if this an incomplete span.
	 */
	long timestamp();

	/**
	 * Measurement in microseconds of the critical path, if known.
	 * Durations of less than one microsecond must be rounded up to 1 microsecond.
	 */
	long duration();

	/**
	 * True if we are contributing to a span started by another tracer (ex on a different host).
	 * Defaults to null. When set, it is expected for kind() to be Kind#SERVER.
	 */
	boolean shared();

	/**
	 * True is a request to store this span even if it overrides sampling policy.
	 */
	boolean debug();

	/**
	 * The host that recorded this span, primarily for query by service name.
	 */
	Endpoint localEndpoint();

	/**
	 * The host that recorded this span, primarily for query by service name.
	 */
	Endpoint remoteEndpoint();

	/**
	 * annotation
	 */
	List<Annotation> annotations();

	/**
	 * tags
	 */
	Map<String ,String> tags();

	String tag(String key);

	default boolean hasError() {
		return tags().containsKey("error");
	}

	default String errorInfo() {
		return tags().get("error");
	}

	// Global
	String type();
	String service();
	String system();

	String localServiceName();
	String remoteServiceName();
}
