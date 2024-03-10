package com.mawen.agent.core.config;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.mawen.agent.config.ConfigManagerMXBean;
import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.core.GlobalAgentHolder;
import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.request.Method;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class PluginPropertyHttpHandler extends AgentHttpHandler {

	ConfigManagerMXBean mxBeanConfig;

	public PluginPropertyHttpHandler() {
		this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
		methods = Collections.singleton(Method.GET);
	}

	@Override
	public String getPath() {
		return "/plugins/domains/:domain/namespaces/:namespace/:id/properties/:property/:value/:version";
	}

	@Override
	public Response process(RouterNanoHTTPD.UriResource uriResource, Map<String, String> urlParams, IHTTPSession session) {
		var version = urlParams.get("version");
		if (version == null) {
			return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, (String) null);
		}
		try {
			var property = ConfigUtils.buildPluginProperty(
					ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null and empty"),
					ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null and empty"),
					ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null and empty"),
					ConfigUtils.requireNonEmpty(urlParams.get("property"), "urlParams.property must not be null and empty")
			);
			var value = Objects.requireNonNull(urlParams.get("value"), "urlParams.value must not be null");
			this.mxBeanConfig.updateService2(Collections.singletonMap(property, value), version);
			return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
		}
		catch (Exception e) {
			return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, e.getMessage());
		}
	}
}
