package com.mawen.agent.core;

import java.net.URLClassLoader;

import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.plugin.report.AgentReport;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class GlobalAgentHolder {

	@Getter @Setter
	private static WrappedConfigManager wrappedConfigManager;

	@Getter @Setter
	private static AgentHttpServer agentHttpServer;

	@Getter @Setter
	private static AgentReport agentReport;

	@Getter @Setter
	private static URLClassLoader agentLoader;
}
