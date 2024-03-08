package com.mawen.agent;

import java.lang.instrument.Instrumentation;

import com.mawen.agent.core.Bootstrap;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/8
 */
public class StartBootstrap {
	private StartBootstrap(){}

	public static void premain(String agentArgs, Instrumentation inst, String javaAgentJarPath) {
		Bootstrap.start(agentArgs, inst, javaAgentJarPath);
	}
}
