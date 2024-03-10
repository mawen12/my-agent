package com.mawen.agent.report.encoder.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.encoder.log.pattern.LogDataPatternFormatter;
import io.opentelemetry.api.trace.SpanContext;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.PatternParser;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 *
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataWriter implements WriteBuffer.Writer<AgentLogData> {

	static final String TYPE_FILED_NAME = "\"type\":\"application-log\"";
	static final String TRACE_ID_FIELD_NAME = ",\"traceId\":\"";
	static final String SPAN_ID_FIELD_NAME = ",\"id\":\"";
	static final String SERVICE_FIELD_NAME = ",\"service\":\"";
	static final String SYSTEM_FIELD_NAME = ",\"system\":\"";
	static final String TIMESTAMP_FIELD_NAME = ",\"timestamp\":\"";
	static final String TIMESTAMP_NUM_FIELD_NAME = ",\"timestamp\":";
	static final String LOG_LEVEL_FIELD_NAME = ",\"logLevel\":\"";
	static final String THREAD_ID_FIELD_NAME = ",\"threadId\":\"";
	static final String LOCATION_FIELD_NAME = ",\"location\":\"";
	static final String MESSAGE_FIELD_NAME = ",\"message\":\"";

	static final String TIMESTAMP = "timestamp";
	static final String LOG_LEVEL = "logLevel";
	static final String THREAD_ID = "threadId";
	static final String LOCATION = "location";
	static final String MESSAGE = "message";

	static final int STATIC_SIZE = 2
			+ TYPE_FILED_NAME.length()
			+ SERVICE_FIELD_NAME.length() + 1
			+ SYSTEM_FIELD_NAME.length() + 1;

	private static final ThreadLocal<StringBuilder> threadLocal = new ThreadLocal<>();

	protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
	protected static final int MAX_STRING_BUILDER_SIZE = 2048;

	Config config;
	PatternParser parser;
	boolean dataTypeIsNumber = false;
	List<LogDataPatternFormatter> dateFormats;
	List<LogDataPatternFormatter> threadIdFormats;
	List<LogDataPatternFormatter> levelFormats;
	List<LogDataPatternFormatter> locationFormats;
	List<LogDataPatternFormatter> messageFormats;

	Map<String, List<LogDataPatternFormatter>> customFields = new HashMap<>();

	public LogDataWriter(Config cfg) {
		this.config = cfg;
		this.parser = PatternLayout.createPatternParser(null);
		initFormatters();
	}


	@Override
	public int sizeInBytes(AgentLogData value) {
		var size = STATIC_SIZE;

		size += JsonEscaper.jsonEscapedSizeInBytes(value.getAgentResource().getService());
		size += JsonEscaper.jsonEscapedSizeInBytes(value.getAgentResource().getSystem());

		if (!value.getSpanContext().equals(SpanContext.getInvalid())) {
			size += TRACE_ID_FIELD_NAME.length() + value.getSpanContext().getTraceId().length() + 1;
			size += SPAN_ID_FIELD_NAME.length() + value.getSpanContext().getSpanId().length() + 1;
		}

		var sb = getStringBuilder();
		if (this.dataTypeIsNumber) {
			size += TIMESTAMP_NUM_FIELD_NAME.length();
			size += WriteBuffer.asciiSizeInBytes(value.getEpochMillis());
		}
		else {
			size += kvLength(TIMESTAMP_FIELD_NAME, value, this.dateFormats, sb, false);
		}

		size += kvLength(LOG_LEVEL_FIELD_NAME, value, this.levelFormats, sb, false);
		size += kvLength(THREAD_ID_FIELD_NAME, value, this.threadIdFormats, sb, false);

		// instrumentInfo - loggerName
		size += kvLength(LOCATION_FIELD_NAME, value, this.locationFormats, sb, false);

		if (!this.customFields.isEmpty()) {
			for (Map.Entry<String, List<LogDataPatternFormatter>> c : this.customFields.entrySet()) {
				size += kvLength(c.getKey(), value, c.getValue(), sb, true);
			}
		}

		size += kvLength(MESSAGE_FIELD_NAME, value, this.messageFormats, sb, true);

		return size;
	}

	@Override
	public void write(AgentLogData value, WriteBuffer b) {
		var sb = getStringBuilder();

		// fix items
		b.writeByte(123);
		b.writeAscii(TYPE_FILED_NAME);

		if (!value.getSpanContext().equals(SpanContext.getInvalid())) {
			// traceId
			b.writeAscii(TRACE_ID_FIELD_NAME);
			b.writeAscii(value.getSpanContext().getTraceId());
			b.writeByte('\"');

			b.writeAscii(SPAN_ID_FIELD_NAME);
			b.writeAscii(value.getSpanContext().getSpanId());
			b.writeByte('\"');
		}

		// resource - system/service
		b.writeAscii(SERVICE_FIELD_NAME);
		b.writeUtf8(JsonEscaper.jsonEscape(value.getAgentResource().getService()));
		b.writeByte('\"');

		b.writeAscii(SYSTEM_FIELD_NAME);
		b.writeUtf8(JsonEscaper.jsonEscape(value.getAgentResource().getSystem()));
		b.writeByte('\"');

		if (this.dataTypeIsNumber) {
			b.writeAscii(TIMESTAMP_NUM_FIELD_NAME);
			b.writeAscii(value.getEpochMillis());
		}
		else {
			writeKeyValue(b, TIMESTAMP_FIELD_NAME,value,this.dateFormats,sb,false);
		}

		writeKeyValue(b, LOG_LEVEL_FIELD_NAME, value, this.dateFormats, sb, false);
		writeKeyValue(b, THREAD_ID_FIELD_NAME, value, this.threadIdFormats, sb, false);

		// instrumentInfo - loggerName
		writeKeyValue(b, LOCATION_FIELD_NAME, value,this.locationFormats,sb,false);

		// attribute and custom
		if (!this.customFields.isEmpty()) {
			for (Map.Entry<String, List<LogDataPatternFormatter>> c : this.customFields.entrySet()) {
				writeKeyValue(b,c.getKey(),value,c.getValue(),sb,true);
			}
		}

		writeKeyValue(b, MESSAGE_FIELD_NAME,value,this.messageFormats, sb, true);

		b.writeByte(125);
	}

	private void initFormatters() {
		this.config.getConfigs().forEach((k, v) -> {
			List<LogDataPatternFormatter> logDataFormatters = LogDataPatternFormatter.transform(v, this.parser);

			switch (k) {
				case LOG_LEVEL -> this.levelFormats = logDataFormatters;
				case THREAD_ID -> this.threadIdFormats = logDataFormatters;
				case LOCATION -> this.locationFormats = logDataFormatters;
				case TIMESTAMP -> {
					this.dateFormats = logDataFormatters;
					this.dataTypeIsNumber = v.equals("%d{UNIX_MILLIS}") || v.equals("%d{UNIX}")
					|| v.equals("%date{UNIX_MILLIS}") || v.equals("%date{UNIX}");
				}
				case MESSAGE -> this.messageFormats = logDataFormatters;
				default -> {
					String key = ",\"" + k + "\":\"";
					this.customFields.put(key, logDataFormatters);
				}
			}
		});
	}

	private int kvLength(String key, AgentLogData value, List<LogDataPatternFormatter> formatters, StringBuilder sb, boolean escape) {
		String d = value.getPatternMap().get(key);
		if (d == null) {
			sb.setLength(0);
			d = toSerializable(value, formatters, sb);
			value.getPatternMap().put(key, d);
		}

		if (StringUtils.isEmpty(d)) {
			return 0;
		}
		else {
			if (escape) {
				return key.length() + JsonEscaper.jsonEscapedSizeInBytes(d) + 1;
			}
			else {
				return key.length() + d.length() + 1;
			}
		}
	}

	private void writeKeyValue(WriteBuffer b, String key, AgentLogData value,
	                           List<LogDataPatternFormatter> formatters,
	                           StringBuilder sb, boolean escape) {
		var d = value.getPatternMap().get(key);
		if (d == null) {
			sb.setLength(0);
			d = toSerializable(value, formatters, sb);
		}

		if (!StringUtils.isEmpty(d)) {
			b.writeAscii(key);
			if (escape) {
				b.writeUtf8(JsonEscaper.jsonEscape(d));
			}
			else {
				b.writeAscii(d);
			}
			b.writeByte('\"');
		}
	}

	private String toSerializable(final AgentLogData value, List<LogDataPatternFormatter> formatters,
	                              final StringBuilder sb) {
		sb.setLength(0);
		for (LogDataPatternFormatter formatter : formatters) {
			formatter.format(value, sb);
		}
		return sb.toString();
	}

	protected static StringBuilder getStringBuilder() {
		var result = threadLocal.get();
		if (result == null) {
			result = new StringBuilder(DEFAULT_STRING_BUILDER_SIZE);
			threadLocal.set(result);
		}
		trimToMaxSize(result, MAX_STRING_BUILDER_SIZE);
		result.setLength(0);
		return result;
	}

	public static void trimToMaxSize(final StringBuilder sb, final int maxSize) {
		if (sb != null && sb.capacity() > maxSize) {
			sb.setLength(maxSize);
			sb.trimToSize();
		}
	}
}
