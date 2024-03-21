package com.mawen.agent.report.encoder.span;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import com.mawen.agent.plugin.report.tracing.ReportSpan;
import org.junit.Test;
import zipkin2.internal.WriteBuffer;

import static org.junit.Assert.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/21
 */
public abstract class AbstractAgentSpanWriterBaseTest {

	private ReportSpan value;
	private String expected;
	private int size;
	private int pos;
	private String bufferStr;

	public AbstractAgentSpanWriterBaseTest(ReportSpan value, String expected) throws Exception {
		this.value = value;
		this.expected = expected;

		WriteBuffer.Writer<ReportSpan> writer = getWriter();

		this.size = writer.sizeInBytes(value);
		size += 2; // { }
		WriteBuffer buffer = WriteBuffer.wrap(new byte[size]);

		// write
		buffer.writeByte('{');
		writer.write(value, buffer);
		buffer.writeByte('}');

		// pos
		Method postMethod = WriteBuffer.class.getDeclaredMethod("pos");
		postMethod.setAccessible(true);

		this.pos = (int) postMethod.invoke(buffer);

		// buffer
		Field bufField = WriteBuffer.class.getDeclaredField("buf");
		bufField.setAccessible(true);

		this.bufferStr = new String((byte[]) bufField.get(buffer));
	}

	@Test
	public void shouldSizeInBytesCorrectly() {
		assertEquals(size, pos);
	}

	@Test
	public void shouldWriteCorrectly() {
		assertEquals(expected, bufferStr);
	}

	public abstract WriteBuffer.Writer<ReportSpan> getWriter();
}
