package com.mawen.agent.httpserver;

import java.io.InputStream;

import org.apache.kafka.common.header.Headers;


public record HttpRequest(Headers headers, String method, String uri, String remoteIp, String remoteHostName, InputStream input) {
}
