package com.mawen.agent.core.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mawen.agent.core.GlobalAgentHolder;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class CanaryListUpdateAgentHttpHandler extends ConfigsUpdateAgentHttpHandler{

	private static final Logger logger = LoggerFactory.getLogger(CanaryListUpdateAgentHttpHandler.class);

	public static final AtomicInteger LAST_COUNT = new AtomicInteger(0);

	public CanaryListUpdateAgentHttpHandler() {
		this.mxBeanConfig = GlobalAgentHolder.getWrappedConfigManager();
	}

	@Override
	public String getPath() {
		return "/config-global-transmission";
	}

	@Override
	public Response processConfig(Map<String, String> config, Map<String, String> urlParams, String version) {
		this.mxBeanConfig.updateCanary2(config, version);
		return null;
	}

	@Override
	public Response processJsonConfig(Map<String, Object> map, Map<String, String> urlParams) {
		logger.info("call /config-global-transmission. configs: {}", map);
		synchronized (LAST_COUNT) {
			var headers = (List<String>) map.get("headers");
			var config = new HashMap<String, String>();
			for (var i = 0; i < headers.size(); i++) {
				config.put("agent.progress.forwarded.headers.global.transmission." + i, headers.get(i));
			}
			var last = LAST_COUNT.get();
			if (headers.size() < last) {
				for (int i = headers.size(); i < last; i++) {
					config.put("agent.progress.forwarded.headers.global.transmission" + i, "");
				}
			}
			LAST_COUNT.set(headers.size());
			this.mxBeanConfig.updateConfigs(config);
		}
		return Response.newFixedLengthResponse(Status.OK, AgentHttpServer.JSON_TYPE, (String) null);
	}
}
