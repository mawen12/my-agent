package com.mawen.agent.profilers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.Stacktrace;
import com.mawen.agent.util.StacktraceMetricBuffer;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class StacktraceCollectorProfiler implements Profiler {

	private long intervalMillis;
	private StacktraceMetricBuffer buffer;
	private String ignoreThreadNamePrefix = "";
	private int maxStringLength = Constants.MAX_STRING_LENGTH;
	private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

	public StacktraceCollectorProfiler(StacktraceMetricBuffer buffer, String ignoreThreadNamePrefix) {
		this(buffer, ignoreThreadNamePrefix, Constants.MAX_STRING_LENGTH);
	}

	public StacktraceCollectorProfiler(StacktraceMetricBuffer buffer, String ignoreThreadNamePrefix, int maxStringLength) {
		this.buffer = buffer;
		this.ignoreThreadNamePrefix = ignoreThreadNamePrefix;
		this.maxStringLength = maxStringLength;
	}

	public void setIntervalMillis(long intervalMillis) {
		this.intervalMillis = intervalMillis;
	}

	@Override
	public long getIntervalMillis() {
		return intervalMillis;
	}

	@Override
	public void setReporter(Reporter reporter) {
	}

	@Override
	public void profile() {
		ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
		if (threadInfos == null) {
			return;
		}

		for (ThreadInfo threadInfo : threadInfos) {
			String threadName = threadInfo.getThreadName();
			if (threadName == null) {
				threadName = "";
			}

			if (!ignoreThreadNamePrefix.isEmpty()
					&& threadName.startsWith(ignoreThreadNamePrefix)) {
				continue;
			}

			StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();

			Stacktrace stacktrace = new Stacktrace();
			stacktrace.setThreadName(threadName);
			stacktrace.setThreadState(String.valueOf(threadInfo.getThreadState()));

			// Start
		}
	}


}
