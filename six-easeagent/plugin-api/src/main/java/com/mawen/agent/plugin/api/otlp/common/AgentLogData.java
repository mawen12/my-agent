package com.mawen.agent.plugin.api.otlp.common;

import java.util.Map;

import com.mawen.agent.plugin.report.EncodedData;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.AgentResource;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface AgentLogData extends LogData {

	String getThreadName();

	String getLocation();

	long getEpochMillis();

	AgentResource getAgentResource();

	void completeAttributes();

	Map<String, String> getPatternMap();

	Throwable getThrowable();

	EncodedData getEncodedData();

	void setEncodedData(EncodedData data);
}
