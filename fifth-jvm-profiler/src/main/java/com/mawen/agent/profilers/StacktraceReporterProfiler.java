package com.mawen.agent.profilers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.reporters.ConsoleOutputReporter;
import com.mawen.agent.util.ClassAndMethod;
import com.mawen.agent.util.Stacktrace;
import com.mawen.agent.util.StacktraceMetricBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class StacktraceReporterProfiler extends ProfilerBase implements Profiler {
	private static final String PROFILER_NAME = "Stacktrace";

	private StacktraceMetricBuffer buffer;

	private Reporter reporter = new ConsoleOutputReporter();

	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;

	public StacktraceReporterProfiler(StacktraceMetricBuffer buffer, Reporter reporter) {
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

		long startEpoch = buffer.getLastResetMillis();

		Map<Stacktrace, AtomicLong> metrics = buffer.reset();

		long endEpoch = buffer.getLastResetMillis();

		for (Map.Entry<Stacktrace, AtomicLong> entry : metrics.entrySet()) {
			Map<String, Object> map = new HashMap<>();

			map.put("startEpoch", startEpoch);
			map.put("endEpoch", endEpoch);

			map.put("host", getHostName());
			map.put("name", getProcessName());
			map.put("processUuid", getProcessUuid());
			map.put("appId", getAppId());

			if (getTag() != null) {
				map.put("tag", getTag());
			}

			if (getCluster() != null) {
				map.put("cluster", getCluster());
			}

			if (getRole() != null) {
				map.put("role", getRole());
			}

			Stacktrace stacktrace = entry.getKey();

			map.put("threadName", stacktrace.getThreadName());
			map.put("threadState", stacktrace.getThreadState());

			ClassAndMethod[] classAndMethodArray = stacktrace.getStack();
			if (classAndMethodArray != null) {
				List<String> stackArray = new ArrayList<>(classAndMethodArray.length);
				for (int i = 0; i < classAndMethodArray.length; i++) {
					ClassAndMethod classAndMethod = classAndMethodArray[i];
					stackArray.add(classAndMethod.getClassName() + "." + classAndMethod.getMethodName());
				}
				map.put("stacktrace", stackArray);
			}

			map.put("count", entry.getValue().get());

			reporter.report(PROFILER_NAME,map);
		}
	}
}
