package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanRemoteEndpointWriter extends AbstractAgentV2SpanEndpointWriter{

	static final String REMOTE_ENDPOINT_FIELD_NAME = """
			,"remoteEndpoint":
			""";

	@Override
	public int sizeInBytes(ReportSpan value) {
		if (value.remoteEndpoint() == null) {
			return 0;
		}
		int size = REMOTE_ENDPOINT_FIELD_NAME.length();
		size += this.endpointSizeInBytes(value.remoteEndpoint(), false);
		return size;
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		if (value.remoteEndpoint() == null) {
			return;
		}
		buffer.writeAscii(REMOTE_ENDPOINT_FIELD_NAME);
		this.writeEndpoint(value.remoteEndpoint(), buffer,false);
	}
}
