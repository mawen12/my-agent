package com.mawen.agent;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("this is an agent.");
		System.out.println("args: " + agentArgs + "\n");
	}
}
