package com.mawen.agent.profilers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.ProcFileUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class IOProfiler extends ProfilerBase implements Profiler {
	private static final String PROFILER_NAME = "IO";

	private long intervalMillis = Constants.DEFAULT_METRIC_INTERVAL;
	private Reporter reporter;

	public IOProfiler(Reporter reporter) {
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
		Map<String, String> procMap = ProcFileUtils.getProcIO();
		Long rchar = ProcFileUtils.getBytesValue(procMap, "rchar");
		Long wchar = ProcFileUtils.getBytesValue(procMap, "wchar");
		Long readBytes = ProcFileUtils.getBytesValue(procMap, "read_bytes");
		Long writeBytes = ProcFileUtils.getBytesValue(procMap, "write_bytes");

		List<Map<String, Object>> cpuTime = ProcFileUtils.getProcStatCpuTime();

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

		Map<String, Object> selfMap = new HashMap<>();
		map.put("self", selfMap);

		Map<String, Object> ioMap = new HashMap<>();
		selfMap.put("io", ioMap);

		ioMap.put("rchar", rchar);
		ioMap.put("wchar", wchar);
		ioMap.put("read_bytes", readBytes);
		ioMap.put("write_bytes", writeBytes);

		map.put("stat", cpuTime);

		if (reporter != null) {
			reporter.report(PROFILER_NAME,map);
		}
	}
}
