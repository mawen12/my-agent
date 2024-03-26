package com.mawen.agent.httpserver;

import java.io.InputStream;

import org.apache.kafka.common.header.Headers;


public class HttpRequest {
	private final Headers headers;
	private final String method;
	private final String uri;
	private final String remoteIp;
	private final String remoteHostName;
	private final InputStream input;

	public HttpRequest(Headers headers, String method, String uri, String remoteIp, String remoteHostName, InputStream input) {
		this.headers = headers;
		this.method = method;
		this.uri = uri;
		this.remoteIp = remoteIp;
		this.remoteHostName = remoteHostName;
		this.input = input;
	}

	public Headers headers() {
		return headers;
	}

	public String method() {
		return method;
	}

	public String uri() {
		return uri;
	}

	public String remoteIp() {
		return remoteIp;
	}

	public String remoteHostName() {
		return remoteHostName;
	}

	public InputStream input() {
		return input;
	}
}
