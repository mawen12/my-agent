package com.mawen.agent.zipkin.impl.message;

import brave.messaging.ProducerRequest;
import com.mawen.agent.plugin.api.trace.MessagingRequest;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class ZipkinProducerRequest<R extends MessagingRequest> extends ProducerRequest {

	private final R request;

	public ZipkinProducerRequest(R request) {
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
