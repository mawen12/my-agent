package com.mawen.agent.core.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.core.GlobalAgentHolder;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class PluginPropertiesHttpHandler extends ConfigsUpdateAgentHttpHandler {

	public PluginPropertiesHttpHandler() {
		this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
	}

	@Override
	public String getPath() {
		return "/plugins/domains/:domain/namespaces/:namespace/:id/properties";
	}

	@Override
	public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
		try {
			var domain = ConfigUtils.requireNonEmpty(urlParams.get("domain"), "urlParams.domain must not be null or empty.");
			var namespace = ConfigUtils.requireNonEmpty(urlParams.get("namespace"), "urlParams.namespace must not be null or empty.");
			var id = ConfigUtils.requireNonEmpty(urlParams.get("id"), "urlParams.id must not be null or empty.");

			var changeConfig = new HashMap<String, String>();
			for (Map.Entry<String, String> entry : config.entrySet()) {
				var key = ConfigUtils.requireNonEmpty(entry.getKey(), "body.key must not be null or empty.");
				var property = ConfigUtils.buildPluginProperty(domain, namespace, id, key);
				var value = Objects.requireNonNull(entry.getValue(), "body.%s must not be null or empty.".formatted(entry.getKey()));
				changeConfig.put(property, value);
			}
			this.mxBeanConfig.updateService2(changeConfig,version);
			return null;
		}
		catch (Exception e) {
			return Response.newFixedLengthResponse(Status.BAD_REQUEST, AgentHttpServer.JSON_TYPE, e.getMessage());
		}
	}
}
