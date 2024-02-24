package com.mawen.agent.plugin.api.trace;

import java.util.function.Function;

import com.mawen.agent.plugin.api.Context;

/**
 * a MessagingTracing
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface MessagingTracing<R extends MessagingRequest> {

	/**
	 * @return {@link Extractor}
	 */
	Extractor<R> producerExtractor();

	/**
	 * @return {@link Extractor}
	 */
	Extractor<R> consumerExtractor();

	/**
	 * @return {@link Injector}
	 */
	Injector<R> producerInjector();

	/**
	 * @return {@link Injector}
	 */
	Injector<R> consumerInjector();

	/**
	 * Returns an overriding sampling decision for a new trace.
	 *
	 * <p>Defaults to ignore the request and use the {@link #producerSampler()} trace ID instead.
	 *
	 * <p>The decision happens when a trace was not yet started in process.
	 * For example, you may be marking an messaging request as a part of booting your application.
	 * You may want to opt-out of tracing producer requests that dii not originate from a consumer request.
	 */
	Function<R, Boolean> producerSampler();

	/**
	 * Returns an overriding sampling decision for a new trace.
	 *
	 * <p>Defaults to ignore the request and use the {@link #consumerSampler()} trace ID instead.
	 *
	 * <p>The decision happens when trace IDS were not in headers, or a sampling decision has not yeet been made.
	 * For example, if a trace is already in progress, this function is not called.
	 * You can implement this to skip channels that you never want to trace.
	 */
	Function<R, Boolean> consumerSampler();

	/**
	 * Create a Span for message producer. Examples: kafka producer, rabbitmq producer.
	 *
	 * <p>It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
	 * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
	 *
	 * <p>It just only pass multiple key:value values required by Trace through {@link Request#setHeader(String, String)},
	 * And set the Span's kind, name and cached scope through {@link Request#kind()} {@link Request#name()} {@link Request#cacheScope()}.
	 * If you need and get Span, generate result use {@link Context#producerSpan(MessagingRequest)}
	 *
	 * @param request {@link MessagingRequest}
	 * @return {@link Span}
	 * @see {@link Span}
	 */
	Span producerSpan(R request);

	/**
	 * Obtain key:value from the message request and create a Span, Example: kafka producer, rabbitmq producer.
	 *
	 * <p>It will set the Span's kind, name and cached scope through
	 * {@link Request#kind()} {@link Request#name()} {@link Request#cacheScope()}.
	 *
	 * <p>It will set the Span's tags "messaging.operation", "messaging.channel_kind", "messaging.channel_name" from request
	 * {@link MessagingRequest#operation()} {@link MessagingRequest#channelKind()} {@link MessagingRequest#channelName()}
	 *
	 * <p>It just only obtain the key:value required by Trace from the {@link Request#header(String)},
	 * If you need and get Span, generate result use {@link Context#consumerSpan(MessagingRequest)}.
	 *
	 * @param request {@link MessagingRequest}
	 * @return {@link Span}
	 * @see {@link Context#consumerSpan(MessagingRequest)}
	 */
	Span consumerSpan(R request);
}
