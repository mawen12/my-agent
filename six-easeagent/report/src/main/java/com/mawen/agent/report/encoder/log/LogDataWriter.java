package com.mawen.agent.report.encoder.log;

import java.util.List;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import org.apache.logging.log4j.core.pattern.PatternParser;
import zipkin2.internal.WriteBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class LogDataWriter implements WriteBuffer.Writer<AgentLogData> {

	static final String TYPE_FILED_NAME = "\"type\":\"application-log\"";
	static final String TRACE_ID_FIELD_NAME = ",\"traceId\":\"";
	static final String SPAN_ID_FIELD_NAME = ",\"id\":\"";
	static final String SERVICE_FIELD_NAME = ",\"service\":\"";
	static final String SYSTEM_FIELD_NAME = ",\"system\":\"";
	static final String TIMESTAMP_FIELD_NAME = ",\"timestamp\":\"";
	static final String TIMESTAMP_NUM_FIELD_NAME = ",\"timestamp\":";
	static final String LOG_LEVEL_FIELD_NAME = ",\"logLevel\":\"";
	static final String THREAD_ID_FIELD_NAME = ",\"threadId\":\"";
	static final String LOCATION_FIELD_NAME = ",\"location\":\"";
	static final String MESSAGE_FIELD_NAME = ",\"message\":\"";

	static final String TIMESTAMP = "timestamp";
	static final String LOG_LEVEL = "logLevel";
	static final String THREAD_ID = "threadId";
	static final String LOCATION = "location";
	static final String MESSAGE = "message";

	static final int STATIC_SIZE = 2
			+ TYPE_FILED_NAME.length()
			+ SERVICE_FIELD_NAME.length() + 1
			+ SYSTEM_FIELD_NAME.length() + 1;

	private static final ThreadLocal<StringBuilder> threadLocal = new ThreadLocal<>();

	protected static final int DEFAULT_STRING_BUILDER_SIZE = 1024;
	protected static final int MAX_STRING_BUILDER_SIZE = 2048;

	Config config;
	PatternParser parser;
	boolean dataTypeIsNumber = false;
	List<LogDataPatternFormatter> dataFormats;


	@Override
	public int sizeInBytes(AgentLogData agentLogData) {
		return 0;
	}

	@Override
	public void write(AgentLogData agentLogData, WriteBuffer writeBuffer) {

	}
}
