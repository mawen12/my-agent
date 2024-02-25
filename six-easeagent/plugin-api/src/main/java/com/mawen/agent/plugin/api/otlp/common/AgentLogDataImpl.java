package com.mawen.agent.plugin.api.otlp.common;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import com.mawen.agent.plugin.report.EncodedData;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.Severity;
import io.opentelemetry.sdk.resources.AgentResource;
import io.opentelemetry.sdk.resources.Resource;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentLogDataImpl implements AgentLogData {
	private AgentResource resource = AgentResource.getResource();
	private InstrumentationLibraryInfo instrumentationLibraryInfo;
	private long epochMillis;
	private SpanContext spanContext;
	private Severity severity;
	private String severityText;
	private String name = null;

	private Body body;
	private Attributes attributes;

	private String threadName;
	private long threadId;
	private Throwable throwable;

	private Map<String, String> patternMap = null;
	private EncodedData encodedData;

	public AgentLogDataImpl(Builder builder) {
		this.epochMillis = builder.epochMills;
		this.spanContext = builder.spanContext == null ? SpanContext.getInvalid() : builder.spanContext;
		this.severity = builder.severity;
		this.severityText = builder.severityText != null ? builder.severityText : this.severity.name();
		this.body = builder.body;

		this.attributes = builder.attributesBuilder != null
				? builder.attributesBuilder.build()
				: AgentAttributes.builder().build();

		this.instrumentationLibraryInfo = AgentInstrumentLibInfo.getInfo(builder.logger);
		this.threadName = builder.threadName;
		this.threadId = builder.threadId;
		this.throwable = builder.throwable;
	}

	@Override
	public String getThreadName() {
		return threadName;
	}

	@Override
	public String getLocation() {
		return this.instrumentationLibraryInfo.getName();
	}

	@Override
	public long getEpochMillis() {
		return this.epochMillis;
	}

	@Override
	public AgentResource getAgentResource() {
		return this.resource;
	}

	@Override
	public void completeAttributes() {
		AttributesBuilder attrsBuilder = this.attributes.toBuilder();
		if (this.throwable != null) {
			attrsBuilder.put(SemanticKey.EXCEPTION_TYPE, throwable.getClass().getName());
			attrsBuilder.put(SemanticKey.EXCEPTION_MESSAGE, throwable.getMessage());

			StringWriter writer = new StringWriter();
			throwable.printStackTrace(new PrintWriter(writer));
			attrsBuilder.put(SemanticKey.EXCEPTION_STACKTRACE, writer.toString());
		}

		attrsBuilder.put(SemanticKey.THREAD_NAME, threadName);
		attrsBuilder.put(SemanticKey.THREAD_ID, threadId);
	}

	@Override
	public Map<String, String> getPatternMap() {
		if (this.patternMap == null) {
			this.patternMap = new HashMap<>();
		}
		return this.patternMap;
	}

	@Override
	public Throwable getThrowable() {
		return this.throwable;
	}

	@Override
	public EncodedData getEncodedData() {
		return this.encodedData;
	}

	@Override
	public void setEncodedData(EncodedData data) {
		this.encodedData = data;
	}

	@Override
	public Resource getResource() {
		return this.resource;
	}

	@Override
	public InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
		return this.instrumentationLibraryInfo;
	}

	@Override
	public long getEpochNanos() {
		return TimeUnit.MICROSECONDS.toNanos(this.epochMillis);
	}

	@Override
	public SpanContext getSpanContext() {
		return this.spanContext;
	}

	@Override
	public Severity getSeverity() {
		return this.severity;
	}

	@Nullable
	@Override
	public String getSeverityText() {
		return this.severityText;
	}

	@Nullable
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Body getBody() {
		return this.body;
	}

	@Override
	public Attributes getAttributes() {
		return this.attributes;
	}

	public static class Builder {
		private String logger;
		private long epochMills;
		private SpanContext spanContext;
		private Severity severity;
		private String severityText;
		private Body body;
		private Throwable throwable;

		private AttributesBuilder attributesBuilder = null;

		private String threadName;
		private long threadId;

		public Builder logger(String logger) {
			this.logger = logger;
			return this;
		}

		public Builder epochMills(long epochMills) {
			this.epochMills = epochMills;
			return this;
		}

		public Builder spanContext() {
			this.spanContext = OtlpSpanContext.getLogSpanContext();
			return this;
		}

		public Builder severity(Severity severity) {
			this.severity = severity;
			return this;
		}

		public Builder severityText(String severityText) {
			this.severityText = severityText;
			return this;
		}

		public Builder body(Body body) {
			this.body = body;
			return this;
		}

		public Builder throwable(Throwable throwable) {
			this.throwable = throwable;
			return this;
		}

		public Builder contextData(Collection<String> keys, Map<String, String> data) {
			if (keys == null || keys.isEmpty()) {
				if (data.isEmpty()) {
					return this;
				}
				keys = data.keySet();
			}

			AttributesBuilder ab = getAttributesBuilder();
			for (String key : keys) {
				ab.put(SemanticKey.stringKey(key), data.get(key));
			}
			return this;
		}

		public AttributesBuilder getAttributesBuilder() {
			if (attributesBuilder == null) {
				attributesBuilder = AgentAttributes.builder();
			}
			return attributesBuilder;
		}

		public AgentLogData build() {
			return new AgentLogDataImpl(this);
		}
	}
}
