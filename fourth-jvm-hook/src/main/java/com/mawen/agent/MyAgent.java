package com.mawen.agent;

import java.lang.instrument.Instrumentation;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.mawen.agent.jvm.Metric;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("This is an perform monitor agent.");

		Executors.newScheduledThreadPool(1).scheduleAtFixedRate(() -> {
			Metric.printMemoryInfo();
			Metric.printGCInfo();
		}, 0, 5000, TimeUnit.MICROSECONDS);
	}
}
