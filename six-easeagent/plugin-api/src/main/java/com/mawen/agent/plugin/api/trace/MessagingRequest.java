package com.mawen.agent.plugin.api.trace;

/**
 * Interface request type used for parsing and sampling of messaging producers and consumers
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface MessagingRequest extends Request {

	/**
	 * The unqualified, case-sensitive semantic message operation name.
	 * The currently defined names are "send" and "receive".
	 *
	 * <p>
	 * examples:
	 * <pre><ul>
	 *     <li>Amazon SQS - {@code AmazonSQS.sendMessageBatch()} is a "send" operation</li>
	 *     <li>JMS - {@code MessageProducer.send()} is a "send" operation</li>
	 *     <li>Kafka - {@code Consumer.poll()} is a "receive" operation</li>
	 *     <li>RabbitMQ - {@code Consumer.handleDelivery()} is a "receive" operation</li>
	 * </ul></pre>
	 * </p>
	 *
	 * <p>Note: There is no constant set of operations, yet. Even when there is a constant set,
	 * there may be operations such as "browse" or "purge" which aren't defined.
	 * Once implementation matures, a constant file will be defined, with potentially more names.
	 *
	 * <p>Conventionally associated with the tag "messaging.operation"
	 *
	 * @return the messaging operation or null if unreadable.
	 */
	String operation();

	/**
	 * Type of channel, e.g. "queue" or "topic", {@code null} if unreadable.
	 *
	 * <p>Conventionally associated with the tag "messaging.channel_name"
	 *
	 * @see #channelName()
	 */
	// Naming matches conventions for Span
	String channelKind();

	/**
	 * Messaging channel name, e.g. "hooks" or "complaints". {@code null} if unreadable.
	 *
	 * <p>Conventionally associated with the tag "messaging.channel_name"
	 *
	 * @see #channelKind()
	 */
	String channelName();

	/**
	 * Returns the underlying request object or {@code null} if there is none.
	 * Here are some request objects: {@code org.apache.http.HttpRequest}, {@code org.apache.dubbo.rpc.Invocation},
	 * {@code org.apache.kafka.clients.consumer.ConsumerRecord}.
	 *
	 * <p>Note: Some implementation are composed of multiple types, such as a request and a socket address of the client.
	 * Moreover, an implementation may change the type returned due to refactoring.
	 * Unless you control the implementation, cast carefully (ex using {@code instanceof})
	 * instead of presuming a specific type will always be returned.
	 */
	Object unwrap();
}