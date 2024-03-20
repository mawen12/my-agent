package com.mawen.agent.zipkin.impl;

import java.util.Map;

import com.mawen.agent.plugin.api.ProgressFields;
import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.Response;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class RequestContextImpl implements RequestContext {

	private final Span span;
	private final Scope scope;
	private final AsyncRequest asyncRequest;

	public RequestContextImpl(Span span, Scope scope, AsyncRequest asyncRequest) {
		this.span = span;
		this.scope = scope;
		this.asyncRequest = asyncRequest;
	}

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public Span span() {
		return span;
	}

	@Override
	public Scope scope() {
		return scope;
	}

	@Override
	public void setHeader(String name, String value) {
		asyncRequest.setHeader(name, value);
	}

	@Override
	public Map<String, String> getHeaders() {
		return asyncRequest.getHeaders();
	}

	@Override
	public void finish(Response response) {
		String[] fields = ProgressFields.getResponseHoldTagFields();
		if (!ProgressFields.isEmpty(fields)) {
			for (String field : fields) {
				span.tag(field, response.header(field));
			}
		}
		span.finish();
	}
}
