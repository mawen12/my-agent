package com.mawen.agent.plugin.api.logging;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.mawen.agent.plugin.report.EncodedData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class AccessLogInfo {
	public static final TypeReference<AccessLogInfo> TYPE_REFERENCE = new TypeReference<>() {};

	@JsonProperty("span_id")
	protected String spanId;

	@JsonProperty("trace_id")
	protected String traceId;

	@JsonProperty("pspan_id")
	protected String parentSpanId;

	private String type = "access-log";
	private String service;
	private String system;
	@JsonProperty("client_ip")
	private String clientIP = "-";

	private String user = "-";

	@JsonProperty("response_size")
	private int responseSize;

	private long beginTime;

	private long beginCpuTime;

	@JsonProperty("request_time")
	private long requestTime;

	private long cpuElapsedTime;

	private String url;

	private String method;

	@JsonProperty("status_code")
	private String statusCode;

	@JsonProperty("host_name")
	private String hostName;

	@JsonProperty("host_ipv4")
	private String hostIpv4;

	private String category = "application";

	@JsonProperty("match_url")
	private String matchUrl;

	private Map<String, String> headers;

	private Map<String, String> queries;

	private long timestamp;

	@JsonIgnore
	private EncodedData encodedData;

	public AccessLogInfo() {}

	public AccessLogInfo(String spanId, String traceId, String parentSpanId, String type, String service, String system, String clientIP, String user, int responseSize, long beginTime, long beginCpuTime, long requestTime, long cpuElapsedTime, String url, String method, String statusCode, String hostName, String hostIpv4, String category, String matchUrl, Map<String, String> headers, Map<String, String> queries, long timestamp, EncodedData encodedData) {
		this.spanId = spanId;
		this.traceId = traceId;
		this.parentSpanId = parentSpanId;
		this.type = type;
		this.service = service;
		this.system = system;
		this.clientIP = clientIP;
		this.user = user;
		this.responseSize = responseSize;
		this.beginTime = beginTime;
		this.beginCpuTime = beginCpuTime;
		this.requestTime = requestTime;
		this.cpuElapsedTime = cpuElapsedTime;
		this.url = url;
		this.method = method;
		this.statusCode = statusCode;
		this.hostName = hostName;
		this.hostIpv4 = hostIpv4;
		this.category = category;
		this.matchUrl = matchUrl;
		this.headers = headers;
		this.queries = queries;
		this.timestamp = timestamp;
		this.encodedData = encodedData;
	}

	public String getSpanId() {
		return spanId;
	}

	public void setSpanId(String spanId) {
		this.spanId = spanId;
	}

	public String getTraceId() {
		return traceId;
	}

	public void setTraceId(String traceId) {
		this.traceId = traceId;
	}

	public String getParentSpanId() {
		return parentSpanId;
	}

	public void setParentSpanId(String parentSpanId) {
		this.parentSpanId = parentSpanId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getSystem() {
		return system;
	}

	public void setSystem(String system) {
		this.system = system;
	}

	public String getClientIP() {
		return clientIP;
	}

	public void setClientIP(String clientIP) {
		this.clientIP = clientIP;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public int getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(int responseSize) {
		this.responseSize = responseSize;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getBeginCpuTime() {
		return beginCpuTime;
	}

	public void setBeginCpuTime(long beginCpuTime) {
		this.beginCpuTime = beginCpuTime;
	}

	public long getRequestTime() {
		return requestTime;
	}

	public void setRequestTime(long requestTime) {
		this.requestTime = requestTime;
	}

	public long getCpuElapsedTime() {
		return cpuElapsedTime;
	}

	public void setCpuElapsedTime(long cpuElapsedTime) {
		this.cpuElapsedTime = cpuElapsedTime;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(String statusCode) {
		this.statusCode = statusCode;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getHostIpv4() {
		return hostIpv4;
	}

	public void setHostIpv4(String hostIpv4) {
		this.hostIpv4 = hostIpv4;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getMatchUrl() {
		return matchUrl;
	}

	public void setMatchUrl(String matchUrl) {
		this.matchUrl = matchUrl;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getQueries() {
		return queries;
	}

	public void setQueries(Map<String, String> queries) {
		this.queries = queries;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public EncodedData getEncodedData() {
		return encodedData;
	}

	public void setEncodedData(EncodedData encodedData) {
		this.encodedData = encodedData;
	}
}
