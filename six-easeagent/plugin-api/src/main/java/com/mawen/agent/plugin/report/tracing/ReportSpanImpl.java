package com.mawen.agent.plugin.report.tracing;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ReportSpanImpl implements ReportSpan{
	String traceId;
	String parentId;
	String id;
	String kind;
	String name;
	long timestamp;
	long duration;
	boolean shared;
	boolean debug;
	Endpoint localEndpoint;
	Endpoint remoteEndpoint;
	List<Annotation> annotations;
	Map<String ,String> tags;

	String type;
	String service;
	String system;

	@Override
	public String traceId() {
		return traceId;
	}

	@Override
	public String parentId() {
		return parentId;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public String kind() {
		return kind;
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public long timestamp() {
		return timestamp;
	}

	@Override
	public long duration() {
		return duration;
	}

	@Override
	public boolean shared() {
		return shared;
	}

	@Override
	public boolean debug() {
		return debug;
	}

	@Override
	public Endpoint localEndpoint() {
		return localEndpoint;
	}

	@Override
	public Endpoint remoteEndpoint() {
		return remoteEndpoint;
	}

	@Override
	public List<Annotation> annotations() {
		return annotations;
	}

	@Override
	public Map<String, String> tags() {
		return tags;
	}

	@Override
	public String tag(String key) {
		return tags != null ? tags.get(key) : null;
	}

	@Override
	public String type() {
		return type;
	}

	@Override
	public String service() {
		return service;
	}

	@Override
	public String system() {
		return system;
	}

	@Override
	public String localServiceName() {
		return localEndpoint != null ? localEndpoint.serviceName() : null;
	}

	@Override
	public String remoteServiceName() {
		return remoteEndpoint != null ? remoteEndpoint.serviceName() : null;
	}

	public ReportSpanImpl(Builder builder) {
		traceId = builder.traceId;
		// prevent self-referencing spans
		parentId = builder.id.equals(builder.parentId) ? null : builder.parentId;
		id = builder.id;
		kind = builder.kind;
		name = builder.name;
		timestamp = builder.timestamp;
		duration = builder.duration;
		shared = builder.shared;
		debug = builder.debug;
		localEndpoint = builder.localEndpoint;
		remoteEndpoint = builder.remoteEndpoint;
		annotations = builder.annotations;
		tags = builder.tags == null ? Collections.emptyMap() : new TreeMap<>(builder.tags);
	}

	public abstract class Builder {
		protected String traceId;
		protected String parentId;
		protected String id;
		protected String kind;
		protected String name;
		protected long timestamp; // zero means null
		protected long duration; // zero means null
		protected boolean shared;
		protected boolean debug;
		protected Endpoint localEndpoint;
		protected Endpoint remoteEndpoint;
		protected List<Annotation> annotations;
		protected Map<String, String> tags;
	}
}
