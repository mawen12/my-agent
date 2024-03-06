package com.mawen.agent.core.config;

import java.util.Map;

import com.mawen.agent.config.CompatibilityConversion;
import com.mawen.agent.core.GlobalAgentHolder;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ServiceUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler {

	public ServiceUpdateAgentHttpHandler() {
		this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
	}

	@Override
	public String getPath() {
		return "/config";
	}

	@Override
	public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
		this.mxBeanConfig.updateService2(CompatibilityConversion.transform(config), version);
		return null;
	}
}
