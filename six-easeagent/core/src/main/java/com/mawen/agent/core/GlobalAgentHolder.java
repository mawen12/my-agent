package com.mawen.agent.core;

import java.net.URLClassLoader;

import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.plugin.report.AgentReport;
import lombok.Getter;
import lombok.Setter;


public class GlobalAgentHolder {

	@Getter @Setter
	private static WrappedConfigManager wrappedConfigManager;

	@Getter @Setter
	private static AgentHttpServer agentHttpServer;

	@Getter @Setter
	private static AgentReport agentReport;

	@Getter @Setter
	private static URLClassLoader agentLoader;

	private GlobalAgentHolder(){}
}
