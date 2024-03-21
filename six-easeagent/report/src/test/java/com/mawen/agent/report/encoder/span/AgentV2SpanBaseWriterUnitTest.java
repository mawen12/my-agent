package com.mawen.agent.report.encoder.span;

import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.Span;
import zipkin2.internal.WriteBuffer;

/**
 * Unit test for {@link AgentV2SpanBaseWriter}
 */
@RunWith(Parameterized.class)
public class AgentV2SpanBaseWriterUnitTest extends AbstractAgentSpanWriterBaseTest {

	public AgentV2SpanBaseWriterUnitTest(ReportSpan value, String expected) throws Exception {
		super(value, expected);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][]{
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120").build(),
						"{\"traceId\":\"0000000000000130\",\"id\":\"0000000000000120\"}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\"}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CLIENT).build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CLIENT\"}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CLIENT).name("name").build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CLIENT\",\"name\":\"name\"}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CLIENT).name("name").timestamp(1).build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CLIENT\",\"name\":\"name\",\"timestamp\":1}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CLIENT).name("name").timestamp(1).duration(2).build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CLIENT\",\"name\":\"name\",\"timestamp\":1,\"duration\":2}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CLIENT).name("name").timestamp(1).duration(2).debug(true).build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CLIENT\",\"name\":\"name\",\"timestamp\":1,\"duration\":2,\"debug\":true}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").parentId("0000000000000100").id("0000000000000120").kind(Span.Kind.CONSUMER).name("name").timestamp(1).duration(2).debug(true).shared(true).build(),
						"{\"traceId\":\"0000000000000130\",\"parentId\":\"0000000000000100\",\"id\":\"0000000000000120\",\"kind\":\"CONSUMER\",\"name\":\"name\",\"timestamp\":1,\"duration\":2,\"debug\":true,\"shared\":true}"},
		});
	}

	@Override
	public WriteBuffer.Writer<ReportSpan> getWriter() {
		return new AgentV2SpanBaseWriter();
	}
}