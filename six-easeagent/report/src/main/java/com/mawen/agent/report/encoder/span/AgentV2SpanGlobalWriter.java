package com.mawen.agent.report.encoder.span;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.util.TextUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.internal.JsonEscaper;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanGlobalWriter implements WriteBuffer.Writer<ReportSpan> {

	static final String TYPE_FIELD_NAME = ",\"type\":\"";
	static final String SERVICE_FIELD_NAME = ",\"service\":\"";
	static final String SYSTEM_FIELD_NAME = ",\"system\":\"";

	final String type;
	final GlobalExtrasSupplier extras;

	public AgentV2SpanGlobalWriter(String type, GlobalExtrasSupplier extras) {
		this.type = type;
		this.extras = extras;
	}

	@Override
	public int sizeInBytes(ReportSpan value) {
		final var mutableInt = new MutableInt(0);
		if (TextUtils.hasText(type)) {
			mutableInt.add(TYPE_FIELD_NAME.length() + 1);
			mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(type));
		}

		var tmpService = this.extras.service();
		if (TextUtils.hasText(tmpService)) {
			mutableInt.add(SERVICE_FIELD_NAME.length() + 1);
			mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpService));
		}

		var tmpSystem = this.extras.system();
		if (TextUtils.hasText(tmpSystem)) {
			mutableInt.add(SYSTEM_FIELD_NAME.length() + 1);
			mutableInt.add(JsonEscaper.jsonEscapedSizeInBytes(tmpSystem));
		}

		return mutableInt.intValue();
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		if (TextUtils.hasText(type)) {
			buffer.writeAscii(TYPE_FIELD_NAME);
			buffer.writeAscii(JsonEscaper.jsonEscapedSizeInBytes(type));
			buffer.writeByte(34);
		}

		var tmpService = this.extras.service();
		if (TextUtils.hasText(tmpService)) {
			buffer.writeAscii(SERVICE_FIELD_NAME);
			buffer.writeAscii(JsonEscaper.jsonEscapedSizeInBytes(tmpService));
			buffer.writeByte(34);
		}

		var tmpSystem = this.extras.system();
		if (TextUtils.hasText(tmpSystem)) {
			buffer.writeAscii(SYSTEM_FIELD_NAME);
			buffer.writeAscii(JsonEscaper.jsonEscapedSizeInBytes(tmpSystem));
			buffer.writeByte(34);
		}
	}
}
