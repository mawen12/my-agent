package com.mawen.agent.core.health;

import java.util.List;
import java.util.Map;

import com.mawen.agent.config.ConfigAware;
import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpHandlerProvider;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.IHTTPSession;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.IStatus;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Response;
import com.mawen.agent.httpserver.nanohttpd.protocols.http.response.Status;
import com.mawen.agent.httpserver.nanohttpd.router.RouterNanoHTTPD;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.health.AgentHealth;
import com.mawen.agent.plugin.bean.AgentInitializingBean;
import com.mawen.agent.plugin.bean.BeanProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class HealthProvider implements AgentHttpHandlerProvider, ConfigAware, AgentInitializingBean, BeanProvider {
	private static final String AGENT_HEALTH_READINESS_ENABLED = "agent.health.readiness.enabled";

	private Config config;

	@Override
	public List<AgentHttpHandler> getAgentHttpHandlers() {
		return List.of(new HealthAgentHttpHandler(),
				new LivenessAgentHttpHandler(),
				new ReadinessAgentHttpHandler());
	}

	@Override
	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void afterPropertiesSet() {
		AgentHealth.setReadinessEnabled(this.config.getBoolean(AGENT_HEALTH_READINESS_ENABLED));
	}



}
