package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanLocalEndpointWriter extends AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<ReportSpan> {

	static final String LOCAL_ENDPOINT_FIELD_NAME = """
			,"localEndpoint":
			""";

	@Override
	public int sizeInBytes(ReportSpan value) {
		if (value.localEndpoint() == null) {
			return 0;
		}
		var size = LOCAL_ENDPOINT_FIELD_NAME.length();
		size += this.endpointSizeInBytes(value.localEndpoint(), true);
		return size;
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		if (value.localEndpoint() == null) {
			return;
		}
		buffer.writeAscii(LOCAL_ENDPOINT_FIELD_NAME);
		this.writeEndpoint(value.localEndpoint(), buffer,true);
	}
}
