package com.mawen.agent.core.health;

import java.util.Map;

import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.mawen.agent.plugin.api.health.AgentHealth;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/1
 */
public class ReadinessAgentHttpHandler extends HealthAgentHttpHandler {

	@Override
	public String getPath() {
		return "/health/readiness";
	}

	@Override
	public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		if (AgentHealth.INSTANCE.isReadinessEnabled()) {
			if (AgentHealth.INSTANCE.isReady()) {
				return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
			}
			return Response.newFixedLengthResponse(HStatus.SERVICE_UNAVAILABLE, AgentHttpServer.JSON_TYPE, (String) null);
		}
		return super.process(uriResource, urlParams, session);
	}
}
