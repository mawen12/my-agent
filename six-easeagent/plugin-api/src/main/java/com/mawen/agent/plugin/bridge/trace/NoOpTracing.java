package com.mawen.agent.plugin.bridge.trace;

import java.util.Collections;
import java.util.List;

import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.MessagingTracing;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.SpanContext;
import com.mawen.agent.plugin.bridge.NoOpContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum NoOpTracing implements ITracing {
	INSTANCE;

	@Override
	public SpanContext exportAsync() {
		return EmptySpanContext.INSTANCE;
	}

	@Override
	public Scope importAsync(SpanContext snapshot) {
		return NoOpScope.INSTANCE;
	}

	@Override
	public RequestContext clientRequest(Request request) {
		return NoOpContext.NO_OP_PROGRESS_CONTEXT;
	}

	@Override
	public RequestContext serverReceive(Request request) {
		return NoOpContext.NO_OP_PROGRESS_CONTEXT;
	}

	@Override
	public List<String> propagationKeys() {
		return Collections.emptyList();
	}

	@Override
	public Span consumerSpan(MessagingRequest request) {
		return NoOpSpan.INSTANCE;
	}

	@Override
	public Span producerSpan(MessagingRequest request) {
		return NoOpSpan.INSTANCE;
	}

	@Override
	public boolean isNoop() {
		return true;
	}

	@Override
	public boolean hasCurrentSpan() {
		return false;
	}

	@Override
	public Span currentSpan() {
		return NoOpSpan.INSTANCE;
	}

	@Override
	public Span nextSpan() {
		return NoOpSpan.INSTANCE;
	}

	@Override
	public MessagingTracing<MessagingRequest> messagingTracing() {
		return EmptyMessagingTracing.INSTANCE;
	}

	@Override
	public Object unwrap() {
		return null;
	}
}
