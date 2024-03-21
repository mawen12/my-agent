package com.mawen.agent.report.encoder.span;

import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.Endpoint;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.internal.WriteBuffer;

/**
 * Unit test for {@link AgentV2SpanLocalEndpointWriter}
 */
@RunWith(Parameterized.class)
public class AgentV2SpanLocalEndpointWriterUnitTest extends AbstractAgentSpanWriterBaseTest {

	public AgentV2SpanLocalEndpointWriterUnitTest(ReportSpan value, String expected) throws Exception {
		super(value, expected);
	}

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][]{
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.localEndpoint(new Endpoint())
						.build(),
						"{}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.localEndpoint(new Endpoint("abc", null, null, 0))
						.build(),
						"{,\"localEndpoint\":{\"serviceName\":\"abc\"}}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.localEndpoint(new Endpoint("abc", "192.168.3.10", null, 0))
						.build(),
						"{,\"localEndpoint\":{\"serviceName\":\"abc\",\"ipv4\":\"192.168.3.10\"}}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.localEndpoint(new Endpoint("abc", "192.168.3.10", "2001:db8:3333:4444:5555:6666:7777:8888", 0))
						.build(),
						"{,\"localEndpoint\":{\"serviceName\":\"abc\",\"ipv4\":\"192.168.3.10\",\"ipv6\":\"2001:db8:3333:4444:5555:6666:7777:8888\"}}"},
				{ReportSpanBuilder.newBuilder().traceId("0000000000000130").id("0000000000000120")
						.localEndpoint(new Endpoint("abc", "192.168.3.10", "2001:db8:3333:4444:5555:6666:7777:8888", 1))
						.build(),
						"{,\"localEndpoint\":{\"serviceName\":\"abc\",\"ipv4\":\"192.168.3.10\",\"ipv6\":\"2001:db8:3333:4444:5555:6666:7777:8888\",\"port\":1}}"},
		});
	}

	@Override
	public WriteBuffer.Writer<ReportSpan> getWriter() {
		return new AgentV2SpanLocalEndpointWriter();
	}
}