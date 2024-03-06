package com.mawen.agent.core.config;

import java.util.Map;

import com.mawen.agent.core.GlobalAgentHolder;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class CanaryUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler{

	public CanaryUpdateAgentHttpHandler() {
		this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
	}

	@Override
	public String getPath() {
		return "/config-canary";
	}

	@Override
	public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
		this.mxBeanConfig.updateCanary2(config, version);
		return null;
	}
}
