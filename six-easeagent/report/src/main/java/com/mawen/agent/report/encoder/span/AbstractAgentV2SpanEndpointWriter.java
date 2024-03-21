package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.Endpoint;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public abstract class AbstractAgentV2SpanEndpointWriter implements WriteBuffer.Writer<ReportSpan> {

	static final String SERVICE_NAME_FIELD_NAME = "\"serviceName\":\"";
	static final String IPV4_FIELD_NAME = "\"ipv4\":\"";
	static final String IPV6_FIELD_NAME = "\"ipv6\":\"";
	static final String PORT_FIELD_NAME = "\"port\":";

	protected int endpointSizeInBytes(Endpoint value, boolean writeEmptyServiceName) {
		var sizeInBytes = 1; // ,

		// serviceName
		var serviceName = value.serviceName();
		if (serviceName == null && writeEmptyServiceName) {
			serviceName = "";
		}

		if (serviceName != null) {
			sizeInBytes += SERVICE_NAME_FIELD_NAME.length() + 1;
			sizeInBytes += JsonEscaper.jsonEscapedSizeInBytes(serviceName);
		}

		// ipv4
		if (value.ipV4() != null) {
			if (sizeInBytes != 1) {
				++sizeInBytes;
			}

			sizeInBytes += IPV4_FIELD_NAME.length() + 1;
			sizeInBytes += value.ipV4().length();
		}

		// ipv6
		if (value.ipV6() != null) {
			if (sizeInBytes != 1) {
				++sizeInBytes;
			}

			sizeInBytes += IPV6_FIELD_NAME.length() + 1;
			sizeInBytes += value.ipV6().length();
		}

		// port
		int port = value.port();
		if (port != 0) {
			if (sizeInBytes != 1) {
				++sizeInBytes;
			}

			sizeInBytes += PORT_FIELD_NAME.length();
			sizeInBytes += WriteBuffer.asciiSizeInBytes(port);
		}

		sizeInBytes += 1;

		return sizeInBytes;
	}

	protected void writeEndpoint(Endpoint value, WriteBuffer b, boolean writeEmptyServiceName) {

		b.writeByte('{');
		var wroteField = false;

		// serviceName
		var serviceName = value.serviceName();
		if (serviceName == null && writeEmptyServiceName) {
			serviceName = "";
		}

		if (serviceName != null) {
			b.writeAscii(SERVICE_NAME_FIELD_NAME);
			b.writeUtf8(JsonEscaper.jsonEscape(serviceName));
			b.writeByte('\"');
			wroteField = true;
		}

		// ipv4
		if (value.ipV4() != null) {
			if (wroteField) {
				b.writeByte(',');
			}
			b.writeAscii(IPV4_FIELD_NAME);
			b.writeAscii(value.ipV4());
			b.writeByte('\"');
			wroteField = true;
		}

		// ipv6
		if (value.ipV6() != null) {
			if (wroteField) {
				b.writeByte(',');
			}
			b.writeAscii(IPV6_FIELD_NAME);
			b.writeAscii(value.ipV6());
			b.writeByte('\"');
			wroteField = true;
		}

		// port
		int port = value.port();
		if (port != 0) {
			if (wroteField) {
				b.writeByte(',');
			}
			b.writeAscii(PORT_FIELD_NAME);
			b.writeAscii(port);
		}

		b.writeByte('}');
	}
}
