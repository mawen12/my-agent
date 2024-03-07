package com.mawen.agent.core;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
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
import com.mawen.agent.plugin.bridge.AgentInfo;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.AgentReportAware;
import com.mawen.agent.report.DefaultAgentReport;
import lombok.SneakyThrows;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class Bootstrap {

	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
	private static final String AGENT_SERVER_PORT_KEY = ConfigFactory.AGENT_SERVER_PORT;
	private static final String AGENT_SERVER_ENABLED_KEY = ConfigFactory.AGENT_SERVER_ENABLED;
	private static final String AGENT_MIDDLEWARE_UPDATE = "agent.middleware.update";
	private static final int DEF_AGENT_SERVER_PORT = 9900;

	private static final AgentBuilder.Listener LISTENER = new AgentBuilder.Listener() {
		@Override
		public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
			// ignored
		}

		@Override
		public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
			log.debug("onTransformation: {} loaded: {} from classloader {}", typeDescription, b, classLoader);
		}

		@Override
		public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
			// ignored
		}

		@Override
		public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
			log.debug("Just for Debug-log, transform ends exceptionally, which is sometimes normal and sometimes there is an error: {} error:{} loaded: {} from classLoader {}",
					s, throwable, b, classLoader);
		}

		@Override
		public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
			// ignored
		}
	};

	static final String MX_BEAN_OBJECT_NAME = "com.mawen.agent:type=ConfigManager";

	private static ContextManager contextManager;

	private Bootstrap(){}

	@SneakyThrows
	public static void start(String args, Instrumentation inst, String javaAgentJarPath) {
		long begin = System.nanoTime();
		System.setProperty(ConfigConst.AGENT_JAR_PATH, javaAgentJarPath);

		// add bootstrap classes
		Set<String> bootstrapClassSet = AppendBootstrapClassLoaderSearch.by(inst, ClassInjector.UsingInstrumentation.Target.BOOTSTRAP);
		if (log.isDebugEnabled()) {
			log.debug("Injected class: {}",bootstrapClassSet);
		}

		// initiate configuration
		String configPath = ConfigFactory.getConfigPath();
		if (StringUtils.isEmpty(configPath)) {
			configPath = args;
		}

		ClassLoader classLoader = Bootstrap.class.getClassLoader();
		AgentInfo agentInfo = AgentInfoFactory.loadAgentInfo(classLoader);
		Agent.agentInfo = agentInfo;
		GlobalConfigs cfg = ConfigFactory.loadConfigs(configPath, classLoader);
		wrapConfig(cfg);

		// loader check
		GlobalAgentHolder.setAgentClassLoader((URLClassLoader) Bootstrap.class.getClassLoader());
		Agent.agentClassLoader = GlobalAgentHolder::getAgentClassLoader;

		// init context/api
		contextManager = ContextManager.build(cfg);
		Agent.dispatcher = new BridgeDispatcher();

		// initInnerHttpServer
		initHttpServer(cfg);

		// redirection
		RedirectProcessor.INSTANCE.init();

		// reporter
		AgentReport agentReport = DefaultAgentReport.create(cfg);
		GlobalAgentHolder.setAgentReport(agentReport);
		Agent.agentReport = agentReport;

		// load plugins
		AgentBuilder builder = getAgentBuilder(cfg, false);
		builder = PluginLoader.load(builder, cfg);

		// provider & beans
		loadProvider(cfg, agentReport);

		long installBegin = System.currentTimeMillis();
		builder.installOn(inst);
		log.info("installBegin use time: {}ms", (System.currentTimeMillis() - installBegin));
		log.info("Initialization has took: {}ns", TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - begin));
	}

	private static void initHttpServer(Configs cfg) {
		// inner httpserver
		Integer port = cfg.getInt(AGENT_SERVER_PORT_KEY);
		if (port == null) {
			port = DEF_AGENT_SERVER_PORT;
		}
		String portStr = System.getProperty(AGENT_SERVER_PORT_KEY, String.valueOf(port));
		port = Integer.parseInt(portStr);

		AgentHttpServer agentHttpServer = new AgentHttpServer(port);

		Boolean httpServerEnabled = cfg.getBoolean(AGENT_SERVER_ENABLED_KEY);
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

	private static void loadProvider(Configs cfg, AgentReport report) {
		List<BeanProvider> providers = BaseLoader.loadOrdered(BeanProvider.class);
		providers.forEach(it -> provider(it, cfg, report));
	}

	private static void provider(BeanProvider provider, Configs cfg, AgentReport report) {
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
		long buildBegin = System.currentTimeMillis();
		AgentBuilder builder = new AgentBuilder.Default()
				.with(LISTENER)
				.with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
				.with(AgentBuilder.InitializationStrategy.NoOp.INSTANCE)
				.with(AgentBuilder.TypeStrategy.Default.REDEFINE)
				.with(AgentBuilder.LocationStrategy.ForClassLoader.STRONG
						.withFallbackTo(ClassFileLocator.ForClassLoader.ofSystemLoader()));

		AgentBuilder.Ignored ignored = builder.ignore(isSynthetic())
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
				.or(nameStartsWith("java\\.lang\\.invoke\\.BoundMethodHandler\\$Species_L.+"))
				.or(nameStartsWith("org.junit."))
				.or(nameStartsWith("junit."))
				.or(nameStartsWith("com.intellij."));

		// config used here to avoid warning of unused
		if (!test && cfg != null) {
			builder = ignored.or(nameStartsWith("com.mawen.agent"));
		} else {
			builder = ignored;
		}

		log.info("AgentBuilder use time: {}ms", (System.currentTimeMillis() - buildBegin));
		return builder;
	}

	@SneakyThrows
	static void registerMBeans(ConfigManagerMXBean conf) {
		long begin = System.currentTimeMillis();
		MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		ObjectName mxBeanName = new ObjectName(MX_BEAN_OBJECT_NAME);
		ClassLoader customClassLoader = Thread.currentThread().getContextClassLoader();
		mbs.registerMBean(conf, mxBeanName);
		log.info("Register {} as MBean {}, use time: {}",
				conf.getClass().getName(), mxBeanName, (System.currentTimeMillis() - begin));
	}

	private static ElementMatcher<ClassLoader> protectedLoaders() {
		return isBootstrapClassLoader().or(is(Bootstrap.class.getClassLoader()));
	}

	private static void wrapConfig(GlobalConfigs conf) {
		WrappedConfigManager wrappedConfigManager = new WrappedConfigManager(Bootstrap.class.getClassLoader(), conf);
		registerMBeans(wrappedConfigManager);
		GlobalAgentHolder.setWrappedConfigManager(wrappedConfigManager);
	}
}
