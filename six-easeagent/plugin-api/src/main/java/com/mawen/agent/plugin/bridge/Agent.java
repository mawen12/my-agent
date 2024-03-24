package com.mawen.agent.plugin.bridge;

import java.net.URLClassLoader;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.IConfigFactory;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.IContextManager;
import com.mawen.agent.plugin.api.dispatcher.IDispatcher;
import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.api.logging.Mdc;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.bridge.metric.NoOpMetricsRegistrySupplier;
import com.mawen.agent.plugin.report.AgentReport;

/**
 * the bridge api will be initialized when agent startup
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public final class Agent {

	public static MetricRegistrySupplier metricRegistrySupplier = NoOpMetricsRegistrySupplier.INSTANCE;
	public static IContextManager initializeContextSupplier = () -> NoOpContext.NO_OP_CONTEXT;
	public static ILoggerFactory loggerFactory = NoOpLoggerFactory.INSTANCE;
	public static Mdc loggerMdc = NoOpMdc.INSTANCE;
	public static IConfigFactory configFactory = NoOpConfigFactory.INSTANCE;
	public static AgentReport agentReport = NoOpAgentReporter.INSTANCE;

	public static IDispatcher dispatcher = NoOpDispatcher.INSTANCE;

	public static Supplier<URLClassLoader> agentClassLoader = () -> null;

	public static void setAgentClassLoader(Supplier<URLClassLoader> agentClassLoader) {
		Agent.agentClassLoader = agentClassLoader;
	}

	public static URLClassLoader getAgentClassLoader() {
		return agentClassLoader.get();
	}

	public static void setDispatcher(IDispatcher dispatcher) {
		Agent.dispatcher = dispatcher;
	}

	public static IDispatcher getDispatcher() {
		return dispatcher;
	}

	/**
	 * @see ILoggerFactory#getLogger(Class)
	 */
	public static Logger getLogger(Class clazz) {
		return loggerFactory.getLogger(clazz);
	}

	/**
	 * @see MetricRegistrySupplier#newMetricRegistry(IPluginConfig, NameFactory, Tags)
	 */
	public static MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
		return metricRegistrySupplier.newMetricRegistry(config, nameFactory, tags);
	}

	public static <T extends ServiceMetric> T getOrCreateMetric(IPluginConfig config,  Tags tags, ServiceMetricSupplier<T> supplier) {
		return ServiceMetricRegistry.getOrCreate(config, tags, supplier);
	}

	/**
	 * @see MetricRegistrySupplier#reporter(IPluginConfig)
	 */
	public static Reporter metricReporter(IPluginConfig config) {
		return metricRegistrySupplier.reporter(config);
	}

	public static Config getConfig() {
		return configFactory.getConfig();
	}

	/**
	 * Return a configuration property from the agent's global configuration.
	 *
	 * @return The configuration of this Java agent.
	 */
	public static String getConfig(String property) {
		return configFactory.getConfig(property);
	}

	/**
	 * find the configuration property from the agent's global configuration.
	 * if not exist, then return {@code defaultValue}.
	 *
	 * @param defaultValue default value returned when the property is not exist
	 * @return The configuration of this Java agent
	 */
	public static String getConfig(String property, String defaultValue) {
		return configFactory.getConfig(property, defaultValue);
	}

	/**
	 * get a Config by domain, namespace and name
	 *
	 * @return {@link IPluginConfig}
	 */
	public static IPluginConfig getConfig(String domain, String namespace, String name) {
		return configFactory.getConfig(domain, namespace, name);
	}

	/**
	 * @return current tracing {@link Context} for session
	 */
	public static Context getContext() {
		return initializeContextSupplier.getContext();
	}
}
