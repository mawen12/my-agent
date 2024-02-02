package com.mawen.agent.reporters;

import java.util.Map;

import com.mawen.agent.Reporter;
import com.mawen.agent.util.JsonUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class ConsoleOutputReporter implements Reporter {

	@Override
	public void report(String profilerName, Map<String, Object> metrics) {
		System.out.println(String.format("ConsoleOutputReporter - %s: %s",profilerName, JsonUtils.serialize(metrics)));
	}

	@Override
	public void close() {

	}
}
