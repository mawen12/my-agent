package com.mawen.agent.core;

import java.net.URLClassLoader;

import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.plugin.report.AgentReport;
import lombok.Getter;
import lombok.Setter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
@Setter
@Getter
public class GlobalAgentHolder {

	private static WrappedConfigManager wrappedConfigManager;
	private static AgentHttpServer agentHttpServer;
	private static AgentReport agentReport;
	private static URLClassLoader agentLoader;

	private GlobalAgentHolder(){}
}
