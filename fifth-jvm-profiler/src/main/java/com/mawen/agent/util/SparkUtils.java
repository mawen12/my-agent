package com.mawen.agent.util;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.mawen.agent.profilers.Constants;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/3
 */
public class SparkUtils {

	public static String probeAppId(String appIdRegex) {
		String appId = System.getProperty("spark.app.id");

		if (appId == null || appId.isEmpty()) {
			String classPath = ProcessUtils.getJvmClassPath();
			List<String> appIdCandidates = StringUtils.extractByRegex(classPath, appIdRegex);
			if (!appIdCandidates.isEmpty()) {
				appId = appIdCandidates.get(0);
			}
		}

		if (appId == null || appId.isEmpty()) {
			for (String entry : ProcessUtils.getJvmInputArguments()) {
				List<String> appIdCandidates = StringUtils.extractByRegex(entry, appIdRegex);
				if (!appIdCandidates.isEmpty()) {
					appId = appIdCandidates.get(0);
					break;
				}
			}
		}

		return appId;
	}

	public static String getSparkEnvAppId() {
		String className = org.apache.commons.lang3.StringUtils.joinWith(
				".",
				"org",
				"apache",
				"spark",
				"SparkEnv");
		try {
			Object result = ReflectionUtils.executeStaticMethods(className, "get.conf.getAppId");
			if (result == null) {
				return null;
			}
			return result.toString();
		}
		catch (Throwable e) {
			return null;
		}
	}

	public static String probeRole(String cmdline) {
		if (ProcessUtils.isSparkExecutor(cmdline)) {
			return Constants.EXECUTOR_ROLE;
		}
		else if (ProcessUtils.isSparkDriver(cmdline)) {
			return Constants.DRIVER_ROLE;
		}
		return null;
	}

	public static SparkAppCmdInfo probeCmdInfo() {
		// TODO use /proc file system to get command when the system property is not available
		String cmd = System.getProperty("sun.java.command");
		if (cmd == null || cmd.isEmpty()) {
			return null;
		}

		SparkAppCmdInfo result = new SparkAppCmdInfo();

		result.setAppJar(StringUtils.getArgumentValue(cmd, "--jar"));
		result.setAppClass(StringUtils.getArgumentValue(cmd, "--class"));

		return result;
	}
}
