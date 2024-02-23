package com.mawen.agent.plugin.api.trace;

import javax.annotation.Nullable;

/**
 * Here's a typical example of synchronous tracing from perspective
 * of the span:
 * <pre>{@code
 *  // Note span methods chain. Explicitly start the span when ready.
 *  Span span = tracer.nextSpan().name("encode").start();
 *  // A span is not responsible for marking itself current (scoped); the tracer is
 *  try (Encoder encoder = getEncoder) {
 *      return encoder.encode();
 *  } catch (RuntimeException | Error e) {
 *      span.error(e); // Unless you handle exceptions, you might not know the operation failed!
 *      throw e;
 *  } finally {
 *      span.finish(); // finish - start = the duration of the operation in microseconds
 *  }
 * }</pre>
 *
 * <p>The captures duration of {@link #start()} util {@link #finish()} is called.</p>
 *
 * <h3>Usage notes</h3>
 * All methods return {@linkplain Span} for chaining, but the instance is always the same.
 * Alse, when only tracing in-process operations.
 *
 * <p>This type can be extended so that the object graph can be built differently or overridden,
 * for example via zipkin or when mocking
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
// Design note: this does not require a builder as the span is mutable anyway. Having a single
// mutation interface is less code to maintain. Those looking to prepare a span before starting it
// can simply call start when they are ready.
public interface Span {

	enum Kind {
		CLIENT,
		SERVER,
		/**
		 * When present, {@link #start()} is the moment a producer sent a
		 * message to a destination. A duration between {@link #start()} and
		 * {@link #finish()} may imply batching delay. {@link #remoteServiceName(String)}
		 * and {@link #remoteIpAndPort(String, int)} indicates the destination, such as a broker.
		 *
		 * <p>Unlike {@link #CLIENT}, messaging spans never share a span ID. For
		 * example, the {@link #CONSUMER} of the same message has {@link Span#parentId()} set to
		 * this span's {@link Span#spanId()}
		 */
		PRODUCER,
		/**
		 * When present, {@link #start()} is the moment a consumer received a
		 * message from an origin. A duration between {@link #start()} and
		 * {@link #finish()} may imply a processing backlog. while
		 * {@link #remoteServiceName(String)} and {@link #remoteIpAndPort(String, int)}
		 * indicates the origin, such as a broker.
		 *
		 * <p>Unlike {@link #SERVER}, messaging spans never share a span ID. For
		 * example, the {@link #PRODUCER} of this message is the {@link #parentId()}
		 * of the span.
		 */
		CONSUMER,
		;
	}

	/**
	 * When true, no recoding is done and nothing is reported.
	 * However, this span should still be injected into outgoing request.
	 * Use this flag to avoid performing expensive computation.
	 */
	boolean isNoop();

	/**
	 * Sets the string name for the logical operation this span represents
	 */
	Span name(String name);

	/**
	 * Tags give your span context for search, viewing and analysis.
	 * For example, a key "your_app.version" would left you lookup spans by version.
	 * A tag "sql.query" isn't searchable, but it can help in debugging when viewing a trace.
	 *
	 * @param key Name used to lookup spans, such as "your_app.version", cannot be null
	 * @param value String value, cannot be null.
	 */
	Span tag(String key, String value);

	/**
	 * Associates an event that explains latency with the current system time.
	 *
	 * @param value A short tag indicating the event, like "finagle.retry"
	 */
	Span annotate(String value);

	/**
	 * Starts the span with an implicit timestamp.
	 * Spans can be modified before calling start.
	 * For example, you can add tags to the span and set its name without lock contention.
	 */
	Span start();

	/**
	 * Like {@link #start()}, expect with a given timestamp in microseconds.
	 * Take extreme care with this feature as it is easy to have incorrect timestamps.
	 * If you must use this.
	 */
	Span start(long timestamp);

	/**
	 * When present, the span is remote. This value clarifies how to interpret
	 * {@link #remoteServiceName(String)} and {@link #remoteIpAndPort(String, int)}
	 */
	Span kind(@Nullable Kind kind);

	/**
	 * Like {@link #annotate(String)}, except with a given timestamp in microseconds.
	 * <p>Take extreme care with this feature as it is easy to have incorrect timestamps.
	 * If you must use this.
	 */
	Span annotate(long timestamp, String value);

	/**
	 * Records an error that impacted this operation.
	 * Note: Calling this does not {@link #finish()} the span.
	 */
	// Design note: <T extends Throwable> T error(T throwable) is tempting but this doesn't work in
	// multi-catch, you should always at least catch RuntimeException and Error.
	Span error(Throwable throwable);

	/**
	 * Lower-case label of the remote node in the service graph,such as "fav-start".
	 * Do not set if unknown. Avoid names with variables or unique identifiers embedded.
	 * <p>
	 * This is a primary label for trace lookup and aggregation, so it should be
	 * intuitive and consistent. Many use a name from service discovery.
	 *
	 * @see {@link #remoteIpAndPort(String, int)}
	 */
	Span remoteServiceName(String remoteServiceName);

	/**
	 * Sets the IP and port associated with the remote endpoint.
	 * For example, the server's listen socket or the connected client socket.
	 * This can also be set to forwarded values, such as an advertised IP.
	 * <p>
	 * Invalid inputs, such as hostnames, will return false.
	 * Port is only set with a valid IP, and zero or negative port values are ignored.
	 * For example, to set only the IP address, leave port as zero.
	 * <p>
	 * This returns boolean, not Span as it is often the case strings are malformed.
	 * Using this, you can do conditional parsing like so:
	 * <pre>{@code
	 *  if (span.remoteIpAndPort(address.getHostAddress(), target, getPort())) {
	 *      return;
	 *  }
	 *  span.remoteIpAndPort(address.getHostName(), target.getPort());
	 * }</pre>
	 * <p>
	 * Note: Comma separated lists are not supported. If you have multiple entries choose
	 * the one most indicative of the remote side. For example, the left-most entry in X-Forwarded-For.
	 *
	 * @param remoteIp the IPv4 or IPv6 literal representing the remote service connection
	 * @param remotePort the port associated with the IP, or zero if unknown.
	 * @see {@link #remoteServiceName(String)}
	 */
	// NOTE: this is remote (IP, port) vs remote IP:port String as zipkin2.Endpoint separates the two,
	// and IP:port strings are uncommon at runtime (even if they are common at config).
	// Parsing IP:port pairs on each request, including concerns like IPv6 bracketing, would add
	// weight for little benefit. If this changes, we can overload it.
 	boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort);

	/**
	 * Throws away the current span without reporting it.
	 */
	void abandon();

	/**
	 * Reports the span complete, assigning the most precise duration possible.
	 */
	void finish();

	/**
	 * Like {@link #finish()}, except with a given timestamp in microseconds.
	 * span duration is derived by subtracting the start timestamp form this, and set when appropriate.
	 * <p>
	 * Take extrame care with this feature as it is easy to have incorrect timestamps.
	 * If you must use this.
	 */
	void finish(long timestamp);

	/**
	 * Reports the span, even if unfinished. Most users will not call this method.
	 *
	 * <p>This primarily supports two use cases: one-way spans and orphaned spans.
	 * For example, a one-way span can be modeled as a span where one tracer calls start and
	 * another calls finish. In order to report that span from its origin, flush must be called.
	 *
	 * <p>Another example is where a user didn't call finish within a deadline or before a shutdown occurs.
	 * By flushing, you can report what was in progress.
	 */
	// a span should not be routinely flushed, only when it has finished, or we don't believe this tracer will finish it.
	void flush();

	/**
	 * Usually calls a {@link Request#setHeader(String, String)} for each propagation field to send downstream.
	 *
	 * @param request holds propagation fields. For example, an outgoing message or http request.
	 */
	void inject(Request request);

	/**
	 * Sets the current span in scope util the returned object is closed.
	 * It is a programming error to drop or never close the result.
	 * Using try-with-resources is preferred for this reason.
	 */
	Scope maybeScope();

	/**
	 * Sets the current span in scope and cache the scope in span util the span is calling {@link #finish()}.
	 * It is a programming error to drop or never close the result.
	 * Using try-with-resources is preferred for this reason.
	 */
	Span cacheScope();

	/**
	 * Returns the hex representation of the span's trace ID
	 */
	String traceIdString();

	/**
	 * Returns the hex representation of the span's ID
	 */
	String spanIdString();

	/**
	 * Returns the hex representation of the span's parent ID
	 */
	String parentIdString();

	/**
	 * Unique 8-byte identifier for a trace, set on all spans within it.
	 */
	Long traceId();

	/**
	 * Unique 8-byte identifier of this span within a trace.
	 *
	 * <p>A span is uniquely identified in storage by ({@linkplain #traceId}).
	 */
	Long spanId();

	/**
	 * The parent's {@link #spanId()} or null if this the root span in a trace.
	 */
	Long parentId();

	/**
	 * Returns the underlying Span object or {@code null} if there is none.
	 * Here is some span objects: {@code brave.LazySpan}, {@code brave.RealSpan}.
	 */
	Object unwrap();
}
