package com.mawen.agent.plugin.tools.metrics;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import com.mawen.agent.plugin.api.ProgressFields;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.utils.SystemClock;
import com.mawen.agent.plugin.utils.common.HostAddress;
import com.mawen.agent.plugin.utils.common.JsonUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class HttpLog {

	public AccessLogInfo prepare(String system, String serviceName, Long beginTime, Span span, AccessLogServerInfo serverInfo) {
		AccessLogInfo accessLog = prepare(system, serviceName, beginTime, serverInfo);
		if (span == null) {
			return accessLog;
		}
		accessLog.setTraceId(span.traceIdString());
		accessLog.setSpanId(span.spanIdString());
		accessLog.setParentSpanId(span.parentIdString());
		return accessLog;
	}

	private AccessLogInfo prepare(String system, String serviceName, Long beginTime, AccessLogServerInfo serverInfo) {
		AccessLogInfo accessLog = new AccessLogInfo();
		accessLog.setSystem(system);
		accessLog.setService(serviceName);
		accessLog.setHostName(HostAddress.localhost());
		accessLog.setHostIpv4(HostAddress.getHostIpv4());
		accessLog.setUrl(serverInfo.getMethod() + " " + serverInfo.getRequestURI());
		accessLog.setMethod(serverInfo.getMethod());
		accessLog.setHeaders(serverInfo.findHeaders());
		accessLog.setBeginTime(beginTime);
		accessLog.setQueries(getQueries(serverInfo));
		accessLog.setClientIP(serverInfo.getClientIP());
		accessLog.setBeginCpuTime(System.nanoTime());
		return accessLog;
	}

	private Map<String, String> getQueries(AccessLogServerInfo serverInfo) {
		Map<String, String> serviceTags = ProgressFields.getServiceTags();
		if (serviceTags.isEmpty()) {
			return serverInfo.findQueries();
		}
		Map<String, String> queries = new HashMap<>();
		queries.putAll(serviceTags);
		queries.putAll(serverInfo.findQueries());
		return queries;
	}

	public String getLogString(AccessLogInfo accessLog, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
		this.finish(accessLog, success, beginTime, serverInfo);
		return JsonUtil.toJson(Lists.newArrayList(accessLog));
	}

	public void finish(AccessLogInfo accessLog, boolean success, Long beginTime, AccessLogServerInfo serverInfo) {
		accessLog.setStatusCode(serverInfo.getStatusCode());
		if (!success) {
			accessLog.setStatusCode("500");
		}
		long now = SystemClock.now();
		accessLog.setTimestamp(now);
		accessLog.setRequestTime(now - beginTime);
		accessLog.setCpuElapsedTime(System.nanoTime() - accessLog.getCpuElapsedTime());
		accessLog.setResponseSize(serverInfo.getResponseBufferSize());
		accessLog.setMatchUrl(serverInfo.getMatchURL());
	}

}
