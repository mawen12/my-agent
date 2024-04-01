package com.mawen.agent.core.health;

import java.util.Map;

import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/1
 */
public class HealthAgentHttpHandler extends AgentHttpHandler {

	@Override
	public String getPath() {
		return "/health";
	}

	@Override
	public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
	}
}
