package com.mawen.agent.plugin.bridge.trace;

import com.mawen.agent.plugin.api.trace.Extractor;
import com.mawen.agent.plugin.api.trace.Message;
import com.mawen.agent.plugin.api.trace.MessagingRequest;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum EmptyExtractor implements Extractor<MessagingRequest> {
	INSTANCE;

	@Override
	public Message extract(MessagingRequest request) {
		return EmptyMessage.INSTANCE;
	}
}
