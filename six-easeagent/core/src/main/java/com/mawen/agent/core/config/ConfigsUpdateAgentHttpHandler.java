package com.mawen.agent.core.config;

import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.config.ConfigManagerMXBean;
import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.mawen.agent.plugin.utils.common.JsonUtil;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public abstract class ConfigsUpdateAgentHttpHandler extends AgentHttpHandler {

	public abstract Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version);

	protected ConfigManagerMXBean mxBeanConfig;

	static Map<String, String> toConfigMap(Map<String, Object> map) {
		var config = new HashMap<String, String>(Math.max(map.size(),8));
		map.forEach((key, value) -> config.put(key, String.valueOf(value)));
		return config;
	}

	@Override
	public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		var body = this.buildRequestBody(session);
		if (StringUtils.isEmpty(body)) {
			return Response.newFixedLengthResponse(Status.BAD_REQUEST,AgentHttpServer.JSON_TYPE, (String) null );
		}
		var map = JsonUtil.toMap(body);
		if (map == null) {
			return Response.newFixedLengthResponse(Status.BAD_REQUEST,AgentHttpServer.JSON_TYPE, (String) null );
		}
		return processJsonConfig(map,urlParams);
	}

	public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
		var config = toConfigMap(map);
		var response = processConfig(config, urlParams, null);
		return response != null ? response : Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
	}
}
