package com.mawen.agent.profilers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.AgentImpl;
import com.mawen.agent.Profiler;
import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;
import com.mawen.agent.util.ProcFileUtils;
import com.mawen.agent.util.ProcessUtils;
import com.mawen.agent.util.SparkAppCmdInfo;
import com.mawen.agent.util.SparkUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class ProcessInfoProfiler extends ProfilerBase implements Profiler {
	private static final AgentLogger logger = AgentLogger.getLogger(ProcessInfoProfiler.class.getName());
	private static final String PROFILER_NAME = "ProcessInfo";

	private String jvmInputArguments = "";
	private String jvmClassPath = "";
	private Long jvmXmxBytes = null;
	private String cmdline = "";

	private Reporter reporter;

	public ProcessInfoProfiler(Reporter reporter) {
		setReporter(reporter);

		init();
	}

	@Override
	public long getIntervalMillis() {
		return 0;
	}

	@Override
	public void setReporter(Reporter reporter) {
		this.reporter = reporter;
	}

	@Override
	public void profile() {
		Map<String, Object> map = new HashMap<>();

		map.put("agentVersion", AgentImpl.VERSION);

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

		// TODO support non spark application
		// TODO also possible to use SparkContext to get spark jar/class info

		SparkAppCmdInfo cmdInfo = SparkUtils.probeCmdInfo();
		if (cmdInfo != null) {
			map.put("appJar", cmdInfo.getAppJar());
			map.put("appClass", cmdInfo.getAppClass());

			// TODO add app arguments
		}

		if (getRole() != null) {
			map.put("role", getRole());
		}

		if (jvmXmxBytes != null) {
			map.put("xmxBytes", jvmXmxBytes);
		}

		String jvmInputArgumentsToReport = jvmInputArguments;
		String jvmClassPathToReport = jvmClassPath;

		// Do not report jvmInputArguments and jvmClassPath if cmdline is not empty.
		// This is because cmdline will contain duplicate information for jvmInputArguments and jvmClassPath
		if (!cmdline.isEmpty()) {
			jvmInputArgumentsToReport = "";
			jvmClassPathToReport = "";
		}

		if (jvmInputArgumentsToReport.length() + jvmClassPathToReport.length() + cmdline.length() <= Constants.MAX_STRING_LENGTH) {
			map.put("jvmInputArguments", jvmInputArgumentsToReport);
			map.put("jvmClassPath", jvmClassPathToReport);
			map.put("cmdline", cmdline);

			if (reporter != null) {
				reporter.report(PROFILER_NAME, map);
			}
		} else {
			List<String> jvmInputArgumentsFragments = com.mawen.agent.util.StringUtils.splitByLength(jvmInputArgumentsToReport, Constants.MAX_STRING_LENGTH);
			List<String> jvmClassPathFragments = com.mawen.agent.util.StringUtils.splitByLength(jvmClassPathToReport, Constants.MAX_STRING_LENGTH);
			List<String> cmdlineFragments = com.mawen.agent.util.StringUtils.splitByLength(cmdline, Constants.MAX_STRING_LENGTH);

			long fragmentSeq = 0;
			long fragmentCount = jvmInputArgumentsFragments.size() + jvmClassPathFragments.size() + cmdlineFragments.size();

			for (String entry : jvmInputArgumentsFragments) {
				Map<String, Object> fragmentMap = createFragmentMap(map, fragmentSeq++, fragmentCount++);
				fragmentMap.put("jvmInputArguments", entry);

				if (reporter != null) {
					reporter.report(PROFILER_NAME, fragmentMap);
				}
			}

			for (String entry : jvmClassPathFragments) {
				Map<String, Object> fragmentMap = createFragmentMap(map, fragmentSeq++, fragmentCount);
				fragmentMap.put("jvmClassPath", entry);

				if (reporter != null) {
					reporter.report(PROFILER_NAME, fragmentMap);
				}
			}

			for (String entry : cmdlineFragments) {
				Map<String, Object> fragmentMap = createFragmentMap(map, fragmentSeq++, fragmentCount);
				fragmentMap.put("cmdline", entry);

				if (reporter != null) {
					reporter.report(PROFILER_NAME,fragmentMap);
				}
			}
		}

	}

	private void init() {
		jvmInputArguments = StringUtils.join(ProcessUtils.getJvmInputArguments(), " ");
		jvmClassPath = ProcessUtils.getJvmClassPath();
		jvmXmxBytes = ProcessUtils.getJvmXmxBytes();

		cmdline = ProcFileUtils.getCmdLine();
		if (cmdline == null) {
			cmdline = " ";
		}
	}

	private Map<String, Object> createFragmentMap(Map<String, Object> copyFrom, long fragmentSeq, long fragmentCount) {
		Map<String, Object> fragmentMap = new HashMap<>();
		fragmentMap.put("fragmentSeq", fragmentSeq);
		fragmentMap.put("fragmentCount", fragmentCount);
		fragmentMap.put("jvmInputArguments", "");
		fragmentMap.put("jvmClassPath", "");
		fragmentMap.put("cmdline", "");

		return fragmentMap;
	}
}
