package com.mawen.agent.mock.context;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.mawen.agent.config.Configs;
import com.mawen.agent.config.PluginConfigManager;
import com.mawen.agent.mock.context.log.LoggerFactoryImpl;
import com.mawen.agent.mock.context.log.LoggerMdc;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.IContextManager;
import com.mawen.agent.plugin.api.logging.ILoggerFactory;
import com.mawen.agent.plugin.api.logging.Mdc;
import com.mawen.agent.plugin.api.metric.MetricProvider;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.TracingProvider;
import com.mawen.agent.plugin.api.trace.TracingSupplier;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.bridge.NoOpLoggerFactory;
import com.mawen.agent.plugin.bridge.NoOpMetrics;
import com.mawen.agent.plugin.bridge.NoOpReporter;
import com.mawen.agent.plugin.bridge.NoOpTracer;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ContextManager implements IContextManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(ContextManager.class.getName());

	private static final ThreadLocal<SessionContext> LOCAL_SESSION_CONTEXT = ThreadLocal.withInitial(SessionContext::new);
	private final PluginConfigManager pluginConfigManager;
	private final Supplier<InitializeContext> sessionContextSupplier;
	private final GlobalContext globalContext;
	private volatile TracingSupplier tracingSupplier = (supplier) -> null;
	private volatile MetricRegistrySupplier metric = NoOpMetrics.NO_OP_METRIC_SUPPLIER;

	public ContextManager(@Nonnull Configs configs, @Nonnull PluginConfigManager pluginConfigManager, @Nonnull ILoggerFactory loggerFactory, @Nonnull Mdc mdc) {
		this.pluginConfigManager = pluginConfigManager;
		this.sessionContextSupplier = new SessionContextSupplier();
		this.globalContext = new GlobalContext(configs, new MetricRegistrySupplierImpl(), loggerFactory, mdc);
	}

	public static ContextManager build(Configs configs) {
		LOGGER.info("build context manager.");
		ProgressFieldsManager.init(configs);
		PluginConfigManager pluginConfigManager = PluginConfigManager.builder(configs).build();
		LoggerFactoryImpl loggerFactory = LoggerFactoryImpl.build();
		ILoggerFactory iLoggerFactory = NoOpLoggerFactory.INSTANCE;
		Mdc mdc = NoOpLoggerFactory.NO_OP_MDC_INSTANCE;
		if (loggerFactory != null) {
			iLoggerFactory = loggerFactory;
			mdc = new LoggerMdc(loggerFactory.factory().mdc());
		}
		ContextManager contextManager = new ContextManager(configs, pluginConfigManager, iLoggerFactory, mdc);
		Agent.loggerFactory = contextManager.globalContext.getLoggerFactory();
		Agent.loggerMdc = contextManager.globalContext.getMdc();
		Agent.initializeContextSupplier = contextManager;
		Agent.metricRegistrySupplier = contextManager.globalContext.getMetric();
		Agent.configFactory = contextManager.pluginConfigManager;
		return contextManager;
	}

	@Override
	public InitializeContext getContext() {
		return this.sessionContextSupplier.get();
	}

	public void setTracing(@Nonnull TracingProvider tracing) {
		LOGGER.info("set tracing supplier function.");
		this.tracingSupplier = tracing.tracingSupplier();
	}

	public void setMetric(@Nonnull MetricProvider metric) {
		LOGGER.info("set metric supplier function");
		this.metric = metric.metricSupplier();
	}


	private class SessionContextSupplier implements Supplier<InitializeContext> {
		@Override
		public InitializeContext get() {
			SessionContext context = LOCAL_SESSION_CONTEXT.get();
			ITracing tracing = context.getTracing();
			if (tracing == null || tracing.isNoop()) {
				context.setCurrentTracing(NoNull.of(tracingSupplier.get(this), NoOpTracer.NO_OP_TRACING));
			}
			if (context.getSupplier() == null) {
				context.setSupplier(this);
			}
			return context;
		}
	}

	private class MetricRegistrySupplierImpl implements MetricRegistrySupplier {

		@Override
		public MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags) {
			return NoNull.of(metric.newMetricRegistry(config, nameFactory, tags), NoOpMetrics.NO_OP_METRIC);
		}

		@Override
		public Reporter reporter(IPluginConfig config) {
			return NoNull.of(metric.reporter(config), NoOpReporter.NO_OP_REPORTER);
		}
	}
}
