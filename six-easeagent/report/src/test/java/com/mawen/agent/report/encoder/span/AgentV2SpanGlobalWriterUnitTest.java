package com.mawen.agent.report.encoder.span;

import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.internal.WriteBuffer;

/**
 * Unit test for {@link AgentV2SpanGlobalWriter}
 */
@RunWith(Parameterized.class)
public class AgentV2SpanGlobalWriterUnitTest extends AbstractAgentSpanWriterBaseTest{

	public AgentV2SpanGlobalWriterUnitTest(ReportSpan value, String expected) throws Exception {
		super(value, expected);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][]{
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.build(),
						"{,\"type\":\"aaa\",\"service\":\"abc\",\"system\":\"bcd\"}"},
		});
	}

	@Override
	public WriteBuffer.Writer<ReportSpan> getWriter() {
		return new AgentV2SpanGlobalWriter("aaa", new GlobalExtrasSupplier(){
			@Override
			public String service() {
				return "abc";
			}

			@Override
			public String system() {
				return "bcd";
			}
		});
	}
}