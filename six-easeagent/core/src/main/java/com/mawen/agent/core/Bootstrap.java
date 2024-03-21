package com.mawen.agent.core;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URLClassLoader;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import com.mawen.agent.config.ConfigAware;
import com.mawen.agent.config.ConfigFactory;
import com.mawen.agent.config.ConfigManagerMXBean;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.GlobalConfigs;
import com.mawen.agent.config.WrappedConfigManager;
import com.mawen.agent.core.config.CanaryListUpdateAgentHttpHandler;
import com.mawen.agent.core.config.CanaryUpdateAgentHttpHandler;
import com.mawen.agent.core.config.PluginPropertiesHttpHandler;
import com.mawen.agent.core.config.PluginPropertyHttpHandler;
import com.mawen.agent.core.config.ServiceUpdateAgentHttpHandler;
import com.mawen.agent.core.info.AgentInfoFactory;
import com.mawen.agent.core.plugin.BaseLoader;
import com.mawen.agent.core.plugin.BridgeDispatcher;
import com.mawen.agent.core.plugin.PluginLoader;
import com.mawen.agent.httpserver.nano.AgentHttpHandlerProvider;
import com.mawen.agent.httpserver.nano.AgentHttpServer;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.mock.context.ContextManager;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.metric.MetricProvider;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.api.trace.TracingProvider;
import com.mawen.agent.plugin.bean.AgentInitializingBean;
import com.mawen.agent.plugin.bean.BeanProvider;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.AgentReportAware;
import com.mawen.agent.report.DefaultAgentReport;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class Bootstrap {
	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

	private static final int DEF_AGENT_SERVER_PORT = 9900;
	static final String MX_BEAN_OBJECT_NAME = "com.mawen.agent:type=ConfigManager";

	private static ContextManager contextManager;

	private static AgentBuilder builder;

	private Bootstrap() {
	}

	public static void start(String args, Instrumentation inst, String javaAgentJarPath) throws IOException {
		var begin = System.currentTimeMillis();
		System.setProperty(ConfigConst.AGENT_JAR_PATH, javaAgentJarPath);

		// add bootstrap classes
		var bootstrapClassSet = AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP);
		log.debugIfEnabled("Injected class: {}", bootstrapClassSet);

		// initiate configuration
		var configPath = ConfigFactory.getConfigPath();
		if (StringUtils.isEmpty(configPath)) {
			configPath = args;
		}

		var classLoader = Bootstrap.class.getClassLoader();
		var cfg = ConfigFactory.loadConfigs(configPath, classLoader);

		wrapConfig(cfg, classLoader);

		initAgentInfo(classLoader); // init AgentInfo

		initAgentClassLoader(classLoader); // init ClassLoader

		initContextManager(cfg); // init context

		initDispatcher(); // init Dispatcher

		initHttpServer(cfg); // init HttpServer

		initRedirection(); // init Redirection

		initReporter(cfg); // init Reporter

		initPlugins(cfg, false); // init Plugins

		initProvider(cfg, Agent.agentReport); // init Provider & Beans

		var installBegin = System.currentTimeMillis();

		builder.installOn(inst);

		log.info("installBegin use time: {}ms", (System.currentTimeMillis() - installBegin));
		log.info("Initialization has took: {}ms", TimeUnit.MILLISECONDS.toMillis(System.currentTimeMillis() - begin));
	}

	private static void initAgentInfo(ClassLoader classLoader) {
		Agent.agentInfo = AgentInfoFactory.loadAgentInfo(classLoader);
	}

	private static void initAgentClassLoader(ClassLoader classLoader) {
		GlobalAgentHolder.setAgentLoader((URLClassLoader) classLoader);
		Agent.agentClassLoader = GlobalAgentHolder::getAgentLoader;
	}

	private static void initContextManager(Configs cfg) {
		contextManager = ContextManager.build(cfg);
	}

	private static void initDispatcher() {
		Agent.dispatcher = new BridgeDispatcher();
	}

	private static void initHttpServer(Configs config) {
		// inner httpserver
		var port = config.getInt(ConfigFactory.AGENT_SERVER_PORT);
		if (port == null) {
			port = DEF_AGENT_SERVER_PORT;
		}
		var portStr = System.getProperty(ConfigFactory.AGENT_SERVER_PORT, String.valueOf(port));
		port = Integer.parseInt(portStr);

		var agentHttpServer = new AgentHttpServer(port);

		var httpServerEnabled = config.getBoolean(ConfigFactory.AGENT_SERVER_ENABLED);
		if (httpServerEnabled) {
			agentHttpServer.startServer();
			log.info("start agent http server on port:{}", port);
		}
		GlobalAgentHolder.setAgentHttpServer(agentHttpServer);

		// add httpHandler
		agentHttpServer.addHttpRoute(new ServiceUpdateAgentHttpHandler());
		agentHttpServer.addHttpRoute(new CanaryUpdateAgentHttpHandler());
		agentHttpServer.addHttpRoute(new CanaryListUpdateAgentHttpHandler());
		agentHttpServer.addHttpRoute(new PluginPropertyHttpHandler());
		agentHttpServer.addHttpRoute(new PluginPropertiesHttpHandler());
	}

	private static void initRedirection() {
		RedirectProcessor.INSTANCE.init();
	}

	private static void initReporter(GlobalConfigs configs) {
		log.info("init reporter >>>>>");
		var agentReport = DefaultAgentReport.create(configs);
		GlobalAgentHolder.setAgentReport(agentReport);
		Agent.agentReport = agentReport;
		log.info("init reporter success!");
	}

	private static void initPlugins(Configs cfg, boolean test) {
		log.info("init plugins >>>>>");
		builder = getAgentBuilder(cfg, false);
		builder = PluginLoader.load(builder);
		log.info("init plugins success!");
	}

	private static void initProvider(Configs cfg, AgentReport report) {
		log.info("init provider >>>>>");
		var providers = BaseLoader.loadOrdered(BeanProvider.class);
		providers.forEach(it -> provider(it, cfg, report));
		log.info("init provider success!");
	}

	private static void provider(BeanProvider provider, Configs cfg, AgentReport report) {
		log.info("Load provider: {}", provider.getClass().getName());
		if (provider instanceof ConfigAware configAware) {
			configAware.setConfig(cfg);
		}
		if (provider instanceof AgentReportAware reportAware) {
			reportAware.setAgentReport(report);
		}
		if (provider instanceof AgentHttpHandlerProvider httpHandlerProvider) {
			GlobalAgentHolder.getAgentHttpServer()
					.addHttpRoutes(httpHandlerProvider.getAgentHttpHandlers());
		}
		if (provider instanceof AgentInitializingBean initializingBean) {
			initializingBean.afterPropertiesSet();
		}
		if (provider instanceof TracingProvider tracingProvider) {
			contextManager.setTracing(tracingProvider);
		}
		if (provider instanceof MetricProvider metricProvider) {
			contextManager.setMetric(metricProvider);
		}
	}

	public static AgentBuilder getAgentBuilder(Configs cfg, boolean test) {
		// config may use to add some classes to be ignored in future
		var buildBegin = System.currentTimeMillis();
		var builder = new AgentBuilder.Default()
				.with(DefaultAgentListener.INSTANCE)
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
				.with(AgentBuilder.TypeStrategy.Default.REDEFINE)
				.with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
						.withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));

		var ignored = builder.ignore(isSynthetic())
				.or(nameStartsWith("sun."))
				.or(nameStartsWith("com.sun."))
				.or(nameStartsWith("brave."))
				.or(nameStartsWith("zipkin2."))
				.or(nameStartsWith("com.fasterxml"))
				.or(nameStartsWith("org.apache.logging")
						.and(not(hasSuperClass(named("org.apache.logging.log4j.spi.AbstractLogger")))))
				.or(nameStartsWith("kotlin."))
				.or(nameStartsWith("javax."))
				.or(nameStartsWith("net.bytebuddy."))
				.or(nameStartsWith("com\\.sun\\.proxy\\.\\$Proxy.+"))
				.or(nameStartsWith("java\\.lang\\.invoke\\.BoundMethodHandle\\$Species_L.+"))
				.or(nameStartsWith("org.junit."))
				.or(nameStartsWith("junit."))
				.or(nameStartsWith("com.intellij."));

		// config used here to avoid warning of unused
		if (!test && cfg != null) {
			builder = ignored.or(nameStartsWith("com.mawen.agent."));
		}
		else {
			builder = ignored;
		}

		log.info("AgentBuilder use time: {}ms", (System.currentTimeMillis() - buildBegin));
		return builder;
	}

	private static void wrapConfig(GlobalConfigs conf, ClassLoader classLoader) {
		var wrappedConfigManager = new WrappedConfigManager(classLoader, conf);
		registerMBeans(wrappedConfigManager);
		GlobalAgentHolder.setWrappedConfigManager(wrappedConfigManager);
	}

	private static void registerMBeans(ConfigManagerMXBean conf) {
		var begin = System.currentTimeMillis();
		ObjectName mxBeanName = null;
		try {
			var mbs = ManagementFactory.getPlatformMBeanServer();
			mxBeanName = new ObjectName(MX_BEAN_OBJECT_NAME);
			mbs.registerMBean(conf, mxBeanName);
			log.info("Register {} as MBean {}, use time: {}", conf.getClass().getName(), mxBeanName, (System.currentTimeMillis() - begin));
		}
		catch (Exception e) {
			log.warn("Register {} as MBean failed, {}", conf.getClass().getName(), e);
			throw new RuntimeException(e);
		}
	}
}
