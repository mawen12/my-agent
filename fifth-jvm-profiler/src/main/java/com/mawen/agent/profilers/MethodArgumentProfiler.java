package com.mawen.agent.profilers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.reporters.ConsoleOutputReporter;
import com.mawen.agent.util.ClassAndMethodArgumentMetricBuffer;
import com.mawen.agent.util.ClassAndMethodMetricKey;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class MethodArgumentProfiler extends ProfilerBase implements Profiler {
	private static final String PROFILER_NAME = "MethodArgument";

	private ClassAndMethodArgumentMetricBuffer buffer;
	private Reporter reporter = new ConsoleOutputReporter();
	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;

	public MethodArgumentProfiler(ClassAndMethodArgumentMetricBuffer buffer, Reporter reporter) {
		this.buffer = buffer;
		this.reporter = reporter;
	}

	@Override
	public long getIntervalMillis() {
		return intervalMillis;
	}

	@Override
	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void profile() {
		if (buffer == null) {
			return;
		}

		if (reporter == null) {
			return;
		}

		Map<ClassAndMethodMetricKey, AtomicLong> metrics = buffer.reset();

		long epochMillis = System.currentTimeMillis();

		for (Map.Entry<ClassAndMethodMetricKey, AtomicLong> entry : metrics.entrySet()) {
			Map<String, Object> commonMap = new HashMap<>();

			commonMap.put("epochMillis", epochMillis);
			commonMap.put("processName", getProcessName());
			commonMap.put("host", getHostName());
			commonMap.put("processUuid", getProcessUuid());
			commonMap.put("appId", getAppId());

			commonMap.put("className", entry.getKey().getClassName());
			commonMap.put("methodName", entry.getKey().getMethodName());

			if (getTag() != null) {
				commonMap.put("tag", getTag());
			}

			if (getCluster() != null) {
				commonMap.put("cluster", getCluster());
			}

			if (getRole() != null) {
				commonMap.put("role", getRole());
			}

			Map<String, Object> metricMap = new HashMap<>();
			metricMap.put("metricName", entry.getKey().getMetricName());
			metricMap.put("metricValue", (double)entry.getValue().get());
			reporter.report(PROFILER_NAME,metricMap);
		}
	}
}
