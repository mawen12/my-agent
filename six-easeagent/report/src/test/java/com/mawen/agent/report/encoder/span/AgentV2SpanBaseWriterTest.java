package com.mawen.agent.report.encoder.span;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.plugin.report.tracing.ReportSpanImpl;
import com.mawen.agent.report.trace.ReportSpanBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import zipkin2.Span;
import zipkin2.internal.WriteBuffer;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class AgentV2SpanBaseWriterTest {

	private ReportSpan value;

	private String excepted;

	public AgentV2SpanBaseWriterTest(ReportSpan value, String excepted) {
		this.value = value;
		this.excepted = excepted;
	}

	@Parameterized.Parameters
	public static Collection<Object[]> params() {
		return Arrays.asList(new Object[][] {
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


	@Test
	public void shouldSizeInBytesCorrectly() throws Exception {
		AgentV2SpanBaseWriter writer = new AgentV2SpanBaseWriter();

		int i = writer.sizeInBytes(value);
		i+=2; // { }
		byte[] result = new byte[i];
		WriteBuffer buffer = WriteBuffer.wrap(result);

		buffer.writeByte('{');
		writer.write(value, buffer);
		buffer.writeByte('}');

		Method method = WriteBuffer.class.getDeclaredMethod("pos");
		method.setAccessible(true);

		assertEquals(i, method.invoke(buffer));
	}

	@Test
	public void shouldWriteCorrectly() throws Exception{
		AgentV2SpanBaseWriter writer = new AgentV2SpanBaseWriter();

		int i = writer.sizeInBytes(value);
		i+=2; // { }
		byte[] result = new byte[i];
		WriteBuffer buffer = WriteBuffer.wrap(result);

		buffer.writeByte('{');
		writer.write(value, buffer);
		buffer.writeByte('}');

		Field field = WriteBuffer.class.getDeclaredField("buf");
		field.setAccessible(true);

		assertEquals(new String((byte[]) field.get(buffer)), excepted);
	}


}