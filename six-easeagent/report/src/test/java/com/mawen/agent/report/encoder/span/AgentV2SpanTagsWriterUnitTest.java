package com.mawen.agent.report.encoder.span;

import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.internal.WriteBuffer;

/**
 * Unit test for {@link AgentV2SpanTagsWriter}
 */
@RunWith(Parameterized.class)
public class AgentV2SpanTagsWriterUnitTest extends AbstractAgentSpanWriterBaseTest{

	public AgentV2SpanTagsWriterUnitTest(ReportSpan value, String expected) throws Exception {
		super(value, expected);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.build(),
						"{}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.putTag("a", "abc")
						.build(),
						"{,\"tags\":{\"a\":\"abc\"}}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.putTag("a", "abc")
						.putTag("b", "bcd")
						.build(),
						"{,\"tags\":{\"a\":\"abc\",\"b\":\"bcd\"}}"},
		});
	}

	@Override
	public WriteBuffer.Writer<ReportSpan> getWriter() {
		return new AgentV2SpanTagsWriter();
	}
}