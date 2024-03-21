package com.mawen.agent.core;

import java.net.URLClassLoader;

import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.plugin.report.AgentReport;


public class GlobalAgentHolder {

	private GlobalAgentHolder() {}

	private static WrappedConfigManager wrappedConfigManager;

	private static AgentHttpServer agentHttpServer;

	private static AgentReport agentReport;

	private static URLClassLoader agentLoader;

	public static WrappedConfigManager getWrappedConfigManager() {
		return wrappedConfigManager;
	}

	public static void setWrappedConfigManager(WrappedConfigManager wrappedConfigManager) {
		GlobalAgentHolder.wrappedConfigManager = wrappedConfigManager;
	}

	public static AgentHttpServer getAgentHttpServer() {
		return agentHttpServer;
	}

	public static void setAgentHttpServer(AgentHttpServer agentHttpServer) {
		GlobalAgentHolder.agentHttpServer = agentHttpServer;
	}

	public static AgentReport getAgentReport() {
		return agentReport;
	}

	public static void setAgentReport(AgentReport agentReport) {
		GlobalAgentHolder.agentReport = agentReport;
	}

	public static URLClassLoader getAgentLoader() {
		return agentLoader;
	}

	public static void setAgentLoader(URLClassLoader agentLoader) {
		GlobalAgentHolder.agentLoader = agentLoader;
	}
}
