package com.mawen.agent.plugin.api.trace;

import java.util.List;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.context.RequestContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface ITracing extends Tracing{

	/**
	 * Export a {@link SpanContext} for async
	 * It will only export the information about the current Span.
	 * If you need SpanContext, generate result use {@link Context#exportAsync()}.
	 *
	 * @return {@link SpanContext}
	 * @see Context#exportAsync()
	 */
	SpanContext exportAsync();

	/**
	 * Import a {@link SpanContext} for async
	 * It will only import the information about the async TraceContext.
	 * If you need import SpanContext and get Scope, generate result use {@link Context#importAsync(AsyncContext)}
	 *
	 * @param snapshot {@link SpanContext}
	 * @return {@link Scope}
	 * @see Context#importAsync(AsyncContext)
	 */
	Scope importAsync(SpanContext snapshot);

	/**
	 * Create a RequestContext for Cross-server Trace link
	 *
	 * <p>It just only pass multiple key:value values required by Trace through
	 * {@link Request#setHeader(String, String)}, And set the Span's kind, name and
	 * cached scope through {@link Request#kind()}, {@link Request#name()} and {@link Request#cacheScope()}.
	 * If you need Cross-process and get RequestContext, generate result use {@link Context#clientRequest(Request)}.
	 *
	 * @param request {@link Request}
	 * @return {@link RequestContext}
	 * @see Context#clientRequest(Request)
	 */
	RequestContext clientRequest(Request request);

	/**
	 * Obtain key:value from the request passed by a parent server and create a RequestContext.
	 *
	 * <p>It will set the Span's kind, name and cached scope through {@link Request#kind()},
	 * {@link Request#name()} and {@link Request#cacheScope()}.
	 *
	 * <p>It just only obtain the key:value required by Trace from the {@link Request#header(String)},
	 * If you need and get RequestContext, generate result use {@link Context#serverReceive(Request)}.
	 *
	 *
	 * @param request {@link Request}
	 * @return {@link RequestContext}
	 * @see Context#serverReceive(Request)
	 */
	RequestContext serverReceive(Request request);

	/**
	 * @return the keys necessary for Span
	 */
	List<String> propagationKeys();

	/**
	 * Obtain key:value from the message request and create a Span, Examples: kafka consumer, rabbitmq consumer
	 *
	 * <p>It will set the Span's kind, name and cached scope through {@link Request#kind()}, {@link Request#name()}
	 * and {@link Request#cacheScope()}.
	 *
	 * <p>It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
	 * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
	 *
	 * <p>It just only obtain the key:value required by Trace from {@link Request#header(String)},
	 * If you need and get Span, generate result use {@link Context#consumerSpan(MessagingRequest)}.
	 *
	 * @param request {@link MessagingRequest}
	 * @return {@link Span}
	 * @see Context#consumerInject(Span, MessagingRequest)
	 */
	Span consumerSpan(MessagingRequest request);

	/**
	 * Create a Span for message producer. Examples: kafka producer, rabbitmq producer
	 *
	 * <p>It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
	 * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
	 *
	 * <p>It just only pass multiple key:value values required by Trace through
	 * {@link Request#setHeader(String, String)}, And set the Span's kind, name and cached scope through
	 * {@link Request#kind()}, {@link Request#name()} and {@link Request#cacheScope()}.
	 * If you need and get Span, generate result use {@link Context#producerSpan(MessagingRequest)}.
	 *
	 * @param request {@link MessagingRequest}
	 * @return {@link Span}
	 * @see Context#producerSpan(MessagingRequest)
	 */
	Span producerSpan(MessagingRequest request);
}
