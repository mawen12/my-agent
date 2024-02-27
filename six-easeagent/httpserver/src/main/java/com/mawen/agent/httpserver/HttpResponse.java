package com.mawen.agent.httpserver;

import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@Data
@Builder
public class HttpResponse {
	private int statusCode;
	private String data;
}
