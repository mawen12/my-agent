package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.Annotation;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanAnnotationsWriter implements WriteBuffer.Writer<ReportSpan> {

	static final String ANNOTATION_FIELD_NAME = ",\"annotations\":[";
	static final String TIMESTAMP_FIELD_NAME = "{\"timestamp\":";
	static final String VALUE_FIELD_NAME = ",\"value\":\"";
	static final String ENDPOINT_FIELD_NAME = ",\"endpoint\":";

	int annotationSizeInBytes(long timestamp, String value, int endpointSizeInBytes) {
		int sizeInBytes = 0;

		sizeInBytes += TIMESTAMP_FIELD_NAME.length();
		sizeInBytes += WriteBuffer.asciiSizeInBytes(timestamp);
		sizeInBytes += VALUE_FIELD_NAME.length() + 1;
		sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(value);
		if (endpointSizeInBytes != 0) {
			sizeInBytes += ENDPOINT_FIELD_NAME.length() + 1;
			sizeInBytes += endpointSizeInBytes;
		}
		sizeInBytes++;
		return sizeInBytes;
	}

	void writeAnnotation(long timestamp, String value, byte[] endpoint, WriteBuffer b) {
		b.writeAscii(TIMESTAMP_FIELD_NAME);
		b.writeAscii(timestamp);
		b.writeAscii(VALUE_FIELD_NAME);
		b.writeUtf8(JsonEscaper.jsonEscape(value));
		b.writeByte(34);// " for value field
		if (endpoint != null) {
			b.writeAscii(ENDPOINT_FIELD_NAME);
			b.write(endpoint);
			b.writeByte(34);// " for value field
		}

		b.writeByte(125); // } for timestamp
	}

	@Override
	public int sizeInBytes(ReportSpan value) {
		int tagCount = 0;
		int sizeInBytes = 0;
		if (!value.annotations().isEmpty()) {
			sizeInBytes += ANNOTATION_FIELD_NAME.length() + 1;
			tagCount = value.annotations().size();
			if (tagCount > 1) {
				sizeInBytes += tagCount - 1; // , for array item
			}

			for (int i = 0; i < tagCount; i++) {
				Annotation a = value.annotations().get(i);
				sizeInBytes += annotationSizeInBytes(a.timestamp(), a.value(), 0);
			}
		}
		return sizeInBytes;
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		if (!value.annotations().isEmpty()) {
			buffer.writeAscii(ANNOTATION_FIELD_NAME);
			int i = 0;
			int length = value.annotations().size();

			while (i < length) {
				Annotation a = value.annotations().get(i++);
				writeAnnotation(a.timestamp(), a.value(), null, buffer);
				if (i < length) {
					buffer.writeByte(44); // , for array item
				}
			}
			buffer.writeByte(93); // ] for annotation field
		}
	}
}
