package com.mawen.agent;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.reporters.ConsoleOutputReporter;
import com.mawen.agent.util.AgentLogger;
import com.mawen.agent.util.ArgumentUtils;
import com.mawen.agent.util.ClassAndMethod;
import com.mawen.agent.util.ClassMethodArgument;
import com.mawen.agent.configprovider.DummyConfigProvider;
import com.mawen.agent.util.JsonUtils;
import com.mawen.agent.util.ReflectionUtils;
import com.mawen.agent.configprovider.YamlConfigProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class Arguments {

	public final static String DEFAULT_APP_ID_REGEX = "application_[\\w_]+";
	public static final long DEFAULT_METRIC_INTERVAL = 60_000;
	public static final long DEFAULT_SAMPLE_INTERVAL = 100;

	public static final String ARG_NOOP = "noop";
	public static final String ARG_REPORTER = "reporter";
	public static final String ARG_CONFIG_PROVIDER = "configProvider";
	public static final String ARG_CONFIG_FILE = "configFile";
	public static final String ARG_METRIC_INTERVAL = "metricInterval";
	public static final String ARG_SAMPLE_INTERVAL = "sampleInterval";
	public static final String ARG_TAG = "tag";
	public static final String ARG_CLUSTER = "cluster";
	public static final String ARG_APP_ID_VARIABLE = "appIdVariable";
	public static final String ARG_APP_ID_REGEX = "appIdRegex";
	public static final String ARG_THREAD_PROFILING = "threadProfiling";
	public static final String ARG_DURATION_PROFILING = "durationProfiling";
	public static final String ARG_ARGUMENT_PROFILING = "argumentProfiling";
	public static final String ARG_IO_PROFILING = "ioProfiling";

	private static final long MIN_INTERVAL_MILLIS = 50;

	private static final AgentLogger logger = AgentLogger.getLogger(Arguments.class.getName());

	private Map<String, List<String>> rawArgValues = new HashMap<>();

	private boolean noop = false;

	private Constructor<Reporter> reporterConstructor;
	private Constructor<ConfigProvider> configProviderConstructor;
	private String configFile;

	private String appIdVariable;
	private String appIdRegex = DEFAULT_APP_ID_REGEX;
	private long metricInterval = DEFAULT_METRIC_INTERVAL;
	private long sampleInterval = 0L;
	private String tag;
	private String cluster;
	private boolean threadProfiling = false;
	private boolean ioProfiling = false;

	private List<ClassAndMethod> durationProfiling = new ArrayList<>();
	private List<ClassMethodArgument> argumentProfiling = new ArrayList<>();

	private Arguments(Map<String, List<String>> parsedArgs) {

	}

	public static Arguments parseArgs(String args) {
		if (args == null) {
			return new Arguments(new HashMap<>());
		}

		args = args.trim();
		if (args.isEmpty()) {
			return new Arguments(new HashMap<>());
		}

		var map = new HashMap<String, List<String>>();
		for (var argPair : args.split(",")) {
			String[] strs = argPair.split("=");
			if (strs.length != 2) {
				throw new IllegalArgumentException("Arguments for the agent should be like: key1=value1,key2=value2");
			}

			var key = strs[0].trim();
			if (key.isEmpty()) {
				throw new IllegalArgumentException("Argument key should not be empty");
			}

			var list = map.computeIfAbsent(key, k -> new ArrayList<>());
			list.add(strs[1].trim());
		}

		return new Arguments(map);
	}

	public void updateArguments(Map<String, List<String>> parsedArgs) {
		rawArgValues.putAll(parsedArgs);

		String argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_NOOP);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			this.noop = Boolean.parseBoolean(argValue);
			logger.info("Got argument value for noop: " + noop);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_REPORTER);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			this.reporterConstructor = ReflectionUtils.getConstructor(argValue, Reporter.class);
			logger.info("Gor argument value for reporter: " + argValue);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_CONFIG_PROVIDER);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			configProviderConstructor = ReflectionUtils.getConstructor(argValue, ConfigProvider.class);
			logger.info("Got argument value for configProvider: " + argValue);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_CONFIG_FILE);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			configFile = argValue;
			logger.info("Got argument value for configFile: " + configFile);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_METRIC_INTERVAL);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			metricInterval = Long.parseLong(argValue);
			logger.info("Got argument value for metricInterval: " + metricInterval);
		}

		if (metricInterval < MIN_INTERVAL_MILLIS) {
			throw new RuntimeException("Metric interval too short, must be at least " + MIN_INTERVAL_MILLIS);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_SAMPLE_INTERVAL);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			sampleInterval = Long.parseLong(argValue);
			logger.info("Got argument value for sampleInterval: " + sampleInterval);
		}

		if (sampleInterval != 0 && sampleInterval < MIN_INTERVAL_MILLIS) {
			throw new RuntimeException("Sample interval too short, must be 0 (disable sampling) or at least " + MIN_INTERVAL_MILLIS);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_TAG);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			tag = argValue;
			logger.info("Got argument value for tag: " + tag);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_CLUSTER);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			cluster = argValue;
			logger.info("Got argument value for cluster: " + cluster);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_APP_ID_VARIABLE);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			appIdVariable = argValue;
			logger.info("Got argument value for appIdVariable: " + appIdVariable);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_APP_ID_REGEX);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			appIdRegex = argValue;
			logger.info("Got argument value for appIdRegex: " + appIdRegex);
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_THREAD_PROFILING);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			threadProfiling = Boolean.parseBoolean(argValue);
			logger.info("GOt argument value for threadProfiling: " + threadProfiling);
		}

		List<String> argValues = ArgumentUtils.getArgumentMultiValues(parsedArgs, ARG_DURATION_PROFILING);
		if (!argValues.isEmpty()) {
			durationProfiling.clear();
			for (String str : argValues) {
				int index = str.lastIndexOf(".");
				if (index <= 0 || index + 1 >= str.length()) {
					throw new IllegalArgumentException("Invalid argument value: " + str);
				}
				String className = str.substring(0, index);
				String methodName = str.substring(index + 1);
				ClassAndMethod classAndMethod = new ClassAndMethod(className, methodName);
				durationProfiling.add(classAndMethod);
				logger.info("Got argument value for durationProfiling: " + classAndMethod);
			}
		}

		argValues = ArgumentUtils.getArgumentMultiValues(parsedArgs, ARG_ARGUMENT_PROFILING);
		if (!argValues.isEmpty()) {
			argumentProfiling.clear();
			for (String str : argValues) {
				int index = str.lastIndexOf(".");
				if (index <= 0 || index + 1 >= str.length()) {
					throw new IllegalArgumentException("Invalid argument value: " + str);
				}
				String classMethodName = str.substring(0, index);
				int argumentIndex = Integer.parseInt(str.substring(index + 1, str.length()));

				index = classMethodName.lastIndexOf(".");
				if (index <= 0 || index + 1 >= classMethodName.length()) {
					throw new IllegalArgumentException("Invalid argument value: " + str);
				}
				String className = classMethodName.substring(0, index);
				String methodName = str.substring(index + 1, classMethodName.length());

				ClassMethodArgument classMethodArgument = new ClassMethodArgument(className, methodName, argumentIndex);
				argumentProfiling.add(classMethodArgument);
				logger.info("Got argument value for argumentProfiling: " + classMethodArgument);
			}
		}

		argValue = ArgumentUtils.getArgumentSingleValue(parsedArgs, ARG_IO_PROFILING);
		if (ArgumentUtils.needToUpdateArg(argValue)) {
			ioProfiling = Boolean.parseBoolean(argValue);
			logger.info("Got argument value for ioProfiling: " + argumentProfiling);
		}
	}

	public void runConfigProvider() {
		try {
			ConfigProvider configProvider = getConfigProvider();
			if (configProvider == null) {
				Map<String, Map<String, List<String>>> extraConfig = configProvider.getConfig();

				Map<String, List<String>> rootConfig = extraConfig.get("");
				if (rootConfig != null) {
					updateArguments(rootConfig);
					logger.info("Updated arguments based on config: " + JsonUtils.serialize(rootConfig));
				}

				// Get tag level config (use tag value to find config values in the config map)
				if (getTag() != null && !getTag().isEmpty()) {
					Map<String, List<String>> overrideConfig = extraConfig.get(getTag());
					if (overrideConfig != null) {
						updateArguments(overrideConfig);
						logger.info("Updated arguments based on config override: " + JsonUtils.serialize(overrideConfig));
					}
				}
			}
		}
		catch (Throwable ex) {
			logger.warn("Failed to update arguments with config provide", ex);
		}
	}

	public Map<String, List<String>> getRawArgValues() {
		return rawArgValues;
	}

	public Reporter getReporter() {
		if (reporterConstructor == null) {
			return new ConsoleOutputReporter();
		}
		else {
			try {
				Reporter reporter = reporterConstructor.newInstance();
				reporter.updateArguments(getRawArgValues());
				return reporter;
			}
			catch (Throwable e) {
				throw new RuntimeException(String.format("Failed to create reporter instance %s", reporterConstructor.getDeclaringClass()), e);
			}
		}
	}

	public ConfigProvider getConfigProvider() {
		if (configProviderConstructor == null) {
			return new DummyConfigProvider();
		}
		else {
			try {
				ConfigProvider configProvider = configProviderConstructor.newInstance();
				if (configProvider instanceof YamlConfigProvider) {
					if (configFile == null || configFile.isEmpty()) {
						throw new RuntimeException("Argument configFile is empty, cannot use " + configProvider.getClass());
					}
					((YamlConfigProvider) configProvider).setFilePath(configFile);
					return configProvider;
				}
				return configProvider;
			}
			catch (Throwable e) {
				throw new RuntimeException(String.format("Failed to create config provider instance %s", configProviderConstructor.getDeclaringClass()), e);
			}
		}
	}

	public boolean isNoop() {
		return noop;
	}

	public void setReporter(String className) {
		reporterConstructor = ReflectionUtils.getConstructor(className, Reporter.class);
	}

	public void setConfigProvider(String className) {
		configProviderConstructor = ReflectionUtils.getConstructor(className, ConfigProvider.class);
	}

	public long getMetricInterval() {
		return metricInterval;
	}

	public long getSampleInterval() {
		return sampleInterval;
	}

	public String getAppIdVariable() {
		return appIdVariable;
	}

	public void setAppIdVariable(String appIdVariable) {
		this.appIdVariable = appIdVariable;
	}

	public String getAppIdRegex() {
		return appIdRegex;
	}

	public String getTag() {
		return tag;
	}

	public String getCluster() {
		return cluster;
	}

	public List<ClassAndMethod> getDurationProfiling() {
		return durationProfiling;
	}

	public List<ClassMethodArgument> getArgumentProfiling() {
		return argumentProfiling;
	}

	public boolean isThreadProfiling() {
		return threadProfiling;
	}

	public boolean isIoProfiling() {
		return ioProfiling;
	}
}
