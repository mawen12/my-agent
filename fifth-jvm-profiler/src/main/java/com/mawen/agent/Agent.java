package com.mawen.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class Agent {

	private static final AgentImpl agentImpl = new AgentImpl();

	private Agent(){}

	public static void agentmain(String args, Instrumentation instrumentation) {
		premain(args, instrumentation);
	}

	public static void premain(String args, Instrumentation instrumentation) {
		System.out.println("Java Agent " + AgentImpl.VERSION + " premain args: " + args);

		Arguments arguments = Arguments.parseArgs(args);
		arguments.runConfigProvider();
		agentImpl.run();
	}

}
