package com.mawen.agent.profilers;

import java.util.HashMap;
import java.util.Map;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.reporters.ConsoleOutputReporter;
import com.mawen.agent.util.ClassAndMethodLongMetricBuffer;
import com.mawen.agent.util.ClassAndMethodMetricKey;
import com.mawen.agent.util.Histogram;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class MethodDurationProfiler extends ProfilerBase implements Profiler {
	private static final String PROFILE_NAME = "MethodDuration";

	private ClassAndMethodLongMetricBuffer buffer;
	private Reporter reporter = new ConsoleOutputReporter();
	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;

	public MethodDurationProfiler(ClassAndMethodLongMetricBuffer buffer, Reporter reporter) {
		this.buffer = buffer;
		this.reporter = reporter;
	}

	@Override
	public long getIntervalMillis() {
		return intervalMillis;
	}

	public void setIntervalMillis(long intervalMillis) {
		this.intervalMillis = intervalMillis;
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

		Map<ClassAndMethodMetricKey, Histogram> metrics = buffer.reset();

		long epochMillis = System.currentTimeMillis();

		for (Map.Entry<ClassAndMethodMetricKey, Histogram> entry : metrics.entrySet()) {
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

			{
				Map<String, Object> metricMap = new HashMap<>(commonMap);
				metricMap.put("metricName", entry.getKey().getMetricName() + ".count");
				metricMap.put("metricValue", (double)entry.getValue().getCount());
				reporter.report(PROFILE_NAME, metricMap);
			}
			{
				Map<String, Object> metricMap = new HashMap<>(commonMap);
				metricMap.put("metricName", entry.getKey().getMetricName() + ".sum");
				metricMap.put("metricValue", (double)entry.getValue().getCount());
				reporter.report(PROFILE_NAME, metricMap);
			}
			{
				Map<String, Object> metricMap = new HashMap<>(commonMap);
				metricMap.put("metricName", entry.getKey().getMetricName() + ".min");
				metricMap.put("metricValue", (double)entry.getValue().getCount());
				reporter.report(PROFILE_NAME, metricMap);
			}
			{
				Map<String, Object> metricMap = new HashMap<>(commonMap);
				metricMap.put("metricName", entry.getKey().getMetricName() + ".max");
				metricMap.put("metricValue", (double)entry.getValue().getCount());
				reporter.report(PROFILE_NAME, metricMap);
			}
		}
	}
}
