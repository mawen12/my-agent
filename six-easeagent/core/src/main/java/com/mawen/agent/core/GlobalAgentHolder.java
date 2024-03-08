package com.mawen.agent.core;

import java.net.URLClassLoader;

import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.plugin.report.AgentReport;


public class GlobalAgentHolder {

	private static WrappedConfigManager wrappedConfigManager;
	private static AgentHttpServer agentHttpServer;
	private static AgentReport agentReport;
	private static URLClassLoader agentLoader;

	public static void setWrappedConfigManager(WrappedConfigManager wrappedConfigManager) {
		GlobalAgentHolder.wrappedConfigManager = wrappedConfigManager;
	}

	public static void setAgentHttpServer(AgentHttpServer agentHttpServer) {
		GlobalAgentHolder.agentHttpServer = agentHttpServer;
	}

	public static void setAgentReport(AgentReport agentReport) {
		GlobalAgentHolder.agentReport = agentReport;
	}

	public static void setAgentClassLoader(URLClassLoader loader) {
		GlobalAgentHolder.agentLoader = loader;
	}

	public static WrappedConfigManager getWrappedConfigManager() {
		return GlobalAgentHolder.wrappedConfigManager;
	}

	public static AgentHttpServer getAgentHttpServer() {
		return GlobalAgentHolder.agentHttpServer;
	}

	public static AgentReport getAgentReport() {
		return GlobalAgentHolder.agentReport;
	}

	public static URLClassLoader getAgentClassLoader() {
		return GlobalAgentHolder.agentLoader;
	}

	private GlobalAgentHolder(){}
}
