package com.mawen.agent.zipkin.impl;

import brave.propagation.TraceContextOrSamplingFlags;
import com.mawen.agent.plugin.api.trace.Message;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class MessageImpl implements Message<TraceContextOrSamplingFlags> {

	private final TraceContextOrSamplingFlags msg;

	public MessageImpl(TraceContextOrSamplingFlags msg) {
		this.msg = msg;
	}

	@Override
	public TraceContextOrSamplingFlags get() {
		return msg;
	}
}
