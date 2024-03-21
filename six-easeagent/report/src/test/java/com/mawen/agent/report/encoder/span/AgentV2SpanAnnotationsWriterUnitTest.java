package com.mawen.agent.report.encoder.span;

import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.internal.WriteBuffer;

/**
 * Unit test for {@link AgentV2SpanAnnotationsWriter}
 */
@RunWith(Parameterized.class)
public class AgentV2SpanAnnotationsWriterUnitTest extends AbstractAgentSpanWriterBaseTest{

	public AgentV2SpanAnnotationsWriterUnitTest(ReportSpan value, String expected) throws Exception {
		super(value, expected);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.build(),
						"{}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.addAnnotation(1, "abc")
						.build(),
						"{,\"annotations\":[{\"timestamp\":1,\"value\":\"abc\"}]}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.addAnnotation(1, "abc")
						.addAnnotation(2,"bcd")
						.build(),
						"{,\"annotations\":[{\"timestamp\":1,\"value\":\"abc\"},{\"timestamp\":2,\"value\":\"bcd\"}]}"},
		});
	}

	@Override
	public WriteBuffer.Writer<ReportSpan> getWriter() {
		return new AgentV2SpanAnnotationsWriter();
	}
}