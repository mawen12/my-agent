package com.mawen.agent.plugin.bridge.trace;

import javax.annotation.Nullable;

import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/24
 */
public enum NoOpSpan implements Span {
	INSTANCE;

	@Override
	public boolean isNoop() {
		return true;
	}

	@Override
	public Span name(String name) {
		return this;
	}

	@Override
	public Span tag(String key, String value) {
		return this;
	}

	@Override
	public Span annotate(String value) {
		return this;
	}

	@Override
	public Span start() {
		return this;
	}

	@Override
	public Span start(long timestamp) {
		return this;
	}

	@Override
	public Span kind(@Nullable Kind kind) {
		return this;
	}

	@Override
	public Span annotate(long timestamp, String value) {
		return this;
	}

	@Override
	public Span error(Throwable throwable) {
		return this;
	}

	@Override
	public Span remoteServiceName(String remoteServiceName) {
		return this;
	}

	@Override
	public boolean remoteIpAndPort(@Nullable String remoteIp, int remotePort) {
		return true;
	}

	@Override
	public void abandon() {
		// NOP
	}

	@Override
	public void finish() {
		// NOP
	}

	@Override
	public void finish(long timestamp) {
		// NOP
	}

	@Override
	public void flush() {
		// NOP
	}

	@Override
	public void inject(Request request) {
		// NOP
	}

	@Override
	public Scope maybeScope() {
		return NoOpScope.INSTANCE;
	}

	@Override
	public Span cacheScope() {
		return this;
	}

	@Override
	public String traceIdString() {
		return "";
	}

	@Override
	public String spanIdString() {
		return "";
	}

	@Override
	public String parentIdString() {
		return "";
	}

	@Override
	public Long traceId() {
		return null;
	}

	@Override
	public Long spanId() {
		return null;
	}

	@Override
	public Long parentId() {
		return null;
	}

	@Override
	public Object unwrap() {
		return null;
	}
}
