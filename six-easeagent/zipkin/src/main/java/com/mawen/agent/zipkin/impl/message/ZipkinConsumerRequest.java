package com.mawen.agent.zipkin.impl.message;

import brave.messaging.ConsumerRequest;
import com.mawen.agent.plugin.api.trace.MessagingRequest;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class ZipkinConsumerRequest<R extends MessagingRequest> extends ConsumerRequest {

	private final R request;

	public ZipkinConsumerRequest(R request) {
		this.request = request;
	}

	@Override
	public String operation() {
		return request.operation();
	}

	@Override
	public String channelKind() {
		return request.channelKind();
	}

	@Override
	public String channelName() {
		return request.channelName();
	}

	@Override
	public Object unwrap() {
		return request.unwrap();
	}
}
