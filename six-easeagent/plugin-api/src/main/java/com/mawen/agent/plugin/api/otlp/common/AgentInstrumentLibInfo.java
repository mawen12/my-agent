package com.mawen.agent.plugin.api.otlp.common;

import java.util.concurrent.ConcurrentHashMap;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AgentInstrumentLibInfo {
	static ConcurrentHashMap<String, InstrumentationLibraryInfo> infoMap = new ConcurrentHashMap<>();

	public static InstrumentationLibraryInfo getInfo(String loggerName) {
		var info = infoMap.get(loggerName);
		if (info != null) {
			return info;
		}
		info = InstrumentationLibraryInfo.create(loggerName, null);
		infoMap.put(loggerName, info);
		return info;
	}
}
