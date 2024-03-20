package com.mawen.agent.zipkin.impl;

import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class AsyncRequest implements Request {
	private final Request request;
	private final Map<String, String> header;

	public AsyncRequest(Request request) {
		this.request = request;
		this.header = new HashMap<>();
	}

	@Override
	public Span.Kind kind() {
		return request.kind();
	}

	@Override
	public String header(String name) {
		String value = request.header(name);
		return value == null ? header.get(name) : value;
	}

	@Override
	public String name() {
		return request.name();
	}

	@Override
	public boolean cacheScope() {
		return request.cacheScope();
	}

	@Override
	public void setHeader(String name, String value) {
		request.setHeader(name, value);
		header.put(name, value);
	}

	public Map<String, String> getHeaders() {
		return header;
	}
}
