package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanTagsWriter implements WriteBuffer.Writer<ReportSpan> {

	@Override
	public int sizeInBytes(ReportSpan value) {
		var sizeInBytes = 0;
		if (!value.tags().isEmpty()) {
			sizeInBytes += 10;
			var iter = value.tags().entrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();
				sizeInBytes += 5;
				sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(entry.getKey());
				sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(entry.getValue());
				if (iter.hasNext()) {
					sizeInBytes += 1;
				}
			}
		}
		return sizeInBytes;
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		if (!value.tags().isEmpty()) {
			buffer.writeAscii(",\"tags\":{");
			var iter = value.tags().entrySet().iterator();
			while (iter.hasNext()) {
				var entry = iter.next();

				buffer.writeByte('\"');
				buffer.writeUtf8(JsonEscaper.jsonEscape(entry.getKey()));
				buffer.writeAscii("\":\"");
				buffer.writeUtf8(JsonEscaper.jsonEscape(entry.getValue()));
				buffer.writeByte('\"');
				if (iter.hasNext()) {
					buffer.writeByte(',');
				}
			}
			buffer.writeByte('}');
		}
	}
}
