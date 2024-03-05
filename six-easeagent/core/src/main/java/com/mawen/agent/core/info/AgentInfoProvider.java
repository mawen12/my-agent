package com.mawen.agent.core.info;

import java.util.List;
import java.util.Map;

import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpHandlerProvider;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.mawen.agent.plugin.bean.BeanProvider;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.utils.common.JsonUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class AgentInfoProvider implements AgentHttpHandlerProvider, BeanProvider {

	@Override
	public List<AgentHttpHandler> getAgentHttpHandlers() {
		return List.of(new AgentInfoHttpHandler());
	}

	public static class AgentInfoHttpHandler extends AgentHttpHandler {

		@Override
		public String getPath() {
			return "/agent-info";
		}

		@Override
		public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
			return Response.newFixedLengthResponse(Status.OK,
					AgentHttpServer.JSON_TYPE,
					JsonUtil.toJson(Agent.getAgentInfo()));
		}
	}
}
