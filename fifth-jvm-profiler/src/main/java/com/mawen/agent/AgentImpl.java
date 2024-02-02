package com.mawen.agent;

import java.io.Closeable;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mawen.agent.util.AgentLogger;
import com.mawen.agent.util.ClassAndMethod;
import com.mawen.agent.util.ClassMethodArgument;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class AgentImpl {
	private static final AgentLogger logger = AgentLogger.getLogger(AgentImpl.class.getName());
	public static final String VERSION = "0.0.1";
	private static final int MAX_THREAD_POOL_SIZE = 2;

	private boolean started = false;

	public void run(Arguments arguments, Instrumentation instrumentation, Collection<AutoCloseable> objectsToCloseOnShutdown) {
		if (arguments.isNoop()) {
			logger.info("Agent noop is true, do not run anything");
			return;
		}

		Reporter reporter = arguments.getReporter();

		String processUuid = UUID.randomUUID().toString();

		String appId = null;

		String appIdVariable = arguments.getAppIdVariable();
		if (appIdVariable != null && !appIdVariable.isEmpty()) {
			appId = System.getenv(appIdVariable);
		}

		if (!arguments.getDurationProfiling().isEmpty()
				|| !arguments.getArgumentProfiling().isEmpty()) {
			instrumentation.addTransformer();

			Set<String> loadedClasses = Arrays.stream(instrumentation.getAllLoadedClasses())
					.map(Class::getName).collect(Collectors.toSet());

			Set<String> tobeReloadClasses = arguments.getDurationProfiling().stream()
					.map(ClassAndMethod::getClassName).collect(Collectors.toSet());

			tobeReloadClasses.addAll(arguments.getArgumentProfiling().stream()
					.map(ClassMethodArgument::getClassName).collect(Collectors.toSet()));

			tobeReloadClasses.retainAll(loadedClasses);

			tobeReloadClasses.forEach(clazz -> {
				try {
					instrumentation.retransformClasses(Class.forName(clazz));
					logger.info("Reload class [" + clazz + "] success.");
				}
				catch (UnmodifiableClassException | ClassNotFoundException e) {
					logger.warn("Reload class [" + clazz + "] failed.", e);
				}
			});


		}
	}

	private List<Profiler> createProfilers(Reporter reporter, Arguments arguments, String processUuid, String appId) {

	}
}
