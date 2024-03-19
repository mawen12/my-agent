package com.mawen.agent.mock.report.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.mock.report.MockSpan;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.report.tracing.Annotation;
import com.mawen.agent.plugin.report.tracing.ReportSpan;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public class ZipkinMockSpanImpl implements MockSpan {

	private static final Map<String, Span.Kind> KINDS;

	static {
		Map<String, Span.Kind> kinds = new HashMap<>();
		kinds.put(zipkin2.Span.Kind.CLIENT.name(), Span.Kind.CLIENT);
		kinds.put(zipkin2.Span.Kind.SERVER.name(), Span.Kind.SERVER);
		kinds.put(zipkin2.Span.Kind.PRODUCER.name(), Span.Kind.PRODUCER);
		kinds.put(zipkin2.Span.Kind.CONSUMER.name(), Span.Kind.CONSUMER);

		KINDS = Collections.unmodifiableMap(kinds);
	}

	private final ReportSpan span;

	public ZipkinMockSpanImpl(ReportSpan span) {
		this.span = span;
	}

	@Override
	public Span.Kind kind() {
		return KINDS.get(span.kind());
	}

	@Override
	public String traceId() {
		return span.traceId();
	}

	@Override
	public String spanId() {
		return span.id();
	}

	@Override
	public String parentId() {
		return span.parentId();
	}

	@Override
	public String tag(String key) {
		Map<String, String> tags = span.tags();
		return tags != null ? tags.get(key) : null;
	}

	@Override
	public Map<String, String> tags() {
		return span.tags();
	}

	@Override
	public String remoteServiceName() {
		return span.remoteEndpoint().serviceName();
	}

	@Override
	public String annotationValueAt(int i) {
		List<Annotation> annotations = span.annotations();
		return annotations != null && annotations.size() >= i ? annotations.get(i).value() : null;
	}

	@Override
	public long timestamp() {
		return span.timestamp();
	}

	@Override
	public Long duration() {
		return span.duration();
	}

	@Override
	public int annotationCount() {
		List<Annotation> annotations = span.annotations();
		return annotations != null ? annotations.size() : 0;
	}

	@Override
	public int remotePort() {
		return span.remoteEndpoint().port();
	}

	@Override
	public int localPort() {
		return span.localEndpoint().port();
	}

	@Override
	public String remoteIp() {
		return span.remoteEndpoint().ipV4();
	}

	@Override
	public String localIp() {
		return span.localEndpoint().ipV4();
	}

	@Override
	public String name() {
		return span.name();
	}

	@Override
	public String localServerName() {
		return span.localServiceName();
	}

	@Override
	public Boolean shared() {
		return span.shared();
	}

	@Override
	public int tagCount() {
		Map<String, String> tags = span.tags();
		return tags != null ? tags.size() : 0;
	}

	@Override
	public boolean hasError() {
		return span.tags().containsKey("error");
	}

	@Override
	public String errorInfo() {
		return span.tags().get("error");
	}
}
