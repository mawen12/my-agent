package com.mawen.agent.httpserver;

import java.io.InputStream;

import lombok.Builder;
import lombok.Data;
import org.apache.kafka.common.header.Headers;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Data
@Builder
public class HttpRequest {
	Headers headers;
	String method;
	String uri;
	String remoteIp;
	String remoteHostName;
	InputStream input;
}
