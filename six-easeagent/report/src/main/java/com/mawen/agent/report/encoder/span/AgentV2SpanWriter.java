package com.mawen.agent.report.encoder.span;

import java.util.Collection;

import com.google.common.collect.ImmutableList;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import org.apache.commons.lang3.mutable.MutableInt;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class AgentV2SpanWriter implements WriteBuffer.Writer<ReportSpan> {

	public final Collection<WriteBuffer.Writer<ReportSpan>> writerList;

	public AgentV2SpanWriter(GlobalExtrasSupplier extrasSupplier) {
		writerList = ImmutableList.<WriteBuffer.Writer<ReportSpan>>builder()
				.add(new AgentV2SpanBaseWriter())
				.add(new AgentV2SpanLocalEndpointWriter())
				.add(new AgentV2SpanRemoteEndpointWriter())
				.add(new AgentV2SpanAnnotationsWriter())
				.add(new AgentV2SpanTagsWriter())
				.add(new AgentV2SpanGlobalWriter("log-tracing", extrasSupplier))
				.build();
	}

	@Override
	public int sizeInBytes(ReportSpan value) {
		final var size = new MutableInt(1);
		writerList.forEach(w -> size.add(w.sizeInBytes(value)));
		size.add(1);
		return size.intValue();
	}

	@Override
	public void write(ReportSpan value, WriteBuffer buffer) {
		buffer.writeByte(123);
		writerList.forEach(w -> w.write(value, buffer));
		buffer.writeByte(125);
	}

	@Override
	public String toString() {
		return "Span";
	}
}
