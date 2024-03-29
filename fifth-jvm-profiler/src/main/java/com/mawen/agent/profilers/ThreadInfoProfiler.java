package com.mawen.agent.profilers;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class ThreadInfoProfiler extends ProfilerBase implements Profiler {
	private static final AgentLogger logger = AgentLogger.getLogger(ThreadInfoProfiler.class.getName());
	private static final String PROFILE_NAME = "ThreadInfo";

	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;

	private ThreadMXBean threadMXBean;
	private long previousTotalStartedThreadCount = 0L; // to keep track of Total Thread.

	private Reporter reporter;

	public ThreadInfoProfiler(Reporter reporter) {
		this.reporter = reporter;

		init();
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
		long totalStartedThreadCount = 0L; // total Thread created so far since Jvm Launch.
		int liveThreadCount = 0; // Number of thread which are currently active.
		int peakThreadCount = 0; // the peak live thread count since the  Java virtual machine started or peak was reset
		long newThreadCount = 0L; // Number of new thread created since last time the metrics was created.
		// This is a Derived metrics from previous data point.
		if (threadMXBean != null) {
			liveThreadCount = threadMXBean.getThreadCount();
			peakThreadCount = threadMXBean.getPeakThreadCount();
			totalStartedThreadCount = threadMXBean.getTotalStartedThreadCount();
			newThreadCount = totalStartedThreadCount - this.previousTotalStartedThreadCount;
			this.previousTotalStartedThreadCount = totalStartedThreadCount;
		}

		Map<String, Object> map = new HashMap<>();

		map.put("epochMillis", System.currentTimeMillis());
		map.put("name", getProcessName());
		map.put("host", getHostName());
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

		map.put("totalStartedThreadCount", totalStartedThreadCount);
		map.put("newThreadCount", newThreadCount);
		map.put("liveThreadCount", liveThreadCount);
		map.put("peakThreadCount", peakThreadCount);

		if (reporter != null) {
			reporter.report(PROFILE_NAME,map);
		}
	}

	private void init() {
		try {
			threadMXBean = ManagementFactory.getThreadMXBean();
		}
		catch (Throwable ex) {
			logger.warn("Failed to get Thread MXBean", ex);
		}
	}
}
