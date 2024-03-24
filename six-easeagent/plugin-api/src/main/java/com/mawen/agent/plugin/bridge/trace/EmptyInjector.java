package com.mawen.agent.plugin.bridge.trace;

import com.mawen.agent.plugin.api.trace.Injector;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum EmptyInjector implements Injector<MessagingRequest> {
	INSTANCE;

	@Override
	public void inject(Span span, MessagingRequest request) {
		// NOP
	}
}
