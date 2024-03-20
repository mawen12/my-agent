package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanBaseWriter implements WriteBuffer.Writer<ReportSpan> {

	static final String TRACE_ID_FIELD_NAME = "\"traceId\":\"";
	static final String PARENT_ID_FIELD_NAME = ",\"parentId\":\"";
	static final String SPAN_ID_FIELD_NAME = ",\"id\":\"";
	static final String KIND_FIELD_NAME = ",\"kind\":\"";
	static final String NAME_FIELD_NAME = ",\"name\":\"";
	static final String TIMESTAMP_FIELD_NAME = ",\"timestamp\":";
	static final String DURATION_FIELD_NAME = ",\"duration\":";
	static final String DEBUG_FIELD_NAME = ",\"debug\":true";
	static final String SHARED_FIELD_NAME = ",\"shared\":true";

	@Override
	public int sizeInBytes(ReportSpan value) {
		var sizeInBytes = 0;

		// traceId
		sizeInBytes += TRACE_ID_FIELD_NAME.length() + 1;
		sizeInBytes += value.traceId().length();

		// parentId
		if (value.parentId() != null) {
			sizeInBytes += PARENT_ID_FIELD_NAME.length() + 1;
			sizeInBytes += value.parentId().length();
		}

		// spanId
		sizeInBytes += SPAN_ID_FIELD_NAME.length() + 1;
		sizeInBytes += value.id().length();

		// kind
		if (value.kind() != null) {
			sizeInBytes += NAME_FIELD_NAME.length() + 1;
			sizeInBytes += value.kind().length();
		}

		// name
		if (value.name() != null) {
			sizeInBytes += NAME_FIELD_NAME.length() + 1;
			sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value.name());
		}

		// timestamp
		if (value.timestamp() != 0L) {
			sizeInBytes += TIMESTAMP_FIELD_NAME.length();
			sizeInBytes += WriteBuffer.asciiSizeInBytes(value.timestamp());
		}

		// duration
		if (value.duration() != 0L) {
			sizeInBytes += DURATION_FIELD_NAME.length();
			sizeInBytes += WriteBuffer.asciiSizeInBytes(value.duration());
		}

		// debug
		if (value.debug()) {
			sizeInBytes += DEBUG_FIELD_NAME.length();
		}

		// shared
		if (value.shared()) {
			sizeInBytes += SHARED_FIELD_NAME.length();
		}

		return sizeInBytes;
	}

	@Override
	public void write(ReportSpan value, WriteBuffer b) {
		// traceId
		b.writeAscii(TRACE_ID_FIELD_NAME);
		b.writeAscii(value.traceId());
		b.writeByte('\"');

		// parentId
		if (value.parentId() != null) {
			b.writeAscii(PARENT_ID_FIELD_NAME);
			b.writeAscii(value.parentId());
			b.writeByte('\"');
		}

		// spanId
		b.writeAscii(SPAN_ID_FIELD_NAME);
		b.writeAscii(value.id());
		b.writeByte(34);

		// kind
		if (value.kind() != null) {
			b.writeAscii(KIND_FIELD_NAME);
			b.writeAscii(value.kind());
			b.writeByte('\"');
		}

		// name
		if (value.name() != null) {
			b.writeAscii(NAME_FIELD_NAME);
			b.writeUtf8(JsonEscaper.jsonEscape(value.name()));
			b.writeByte('\"');
		}

		// timestamp
		if (value.timestamp() != 0L) {
			b.writeAscii(TIMESTAMP_FIELD_NAME);
			b.writeAscii(value.timestamp());
		}

		// duration
		if (value.duration() != 0L) {
			b.writeAscii(DURATION_FIELD_NAME);
			b.writeAscii(value.duration());
		}

		// debug
		if (Boolean.TRUE.equals(value.debug())) {
			b.writeAscii(DEBUG_FIELD_NAME);
		}

		// shared
		if (Boolean.TRUE.equals(value.shared())) {
			b.writeAscii(SHARED_FIELD_NAME);
		}
	}
}
