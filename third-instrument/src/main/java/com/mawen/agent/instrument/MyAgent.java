package com.mawen.agent.instrument;

import java.lang.instrument.Instrumentation;

import com.mawen.agent.instrument.transformer.PerformMonitorTransformer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("This is an perform monitor agent.");

		// 添加 Transformer
		PerformMonitorTransformer transformer = new PerformMonitorTransformer();
		inst.addTransformer(transformer);
	}
}
