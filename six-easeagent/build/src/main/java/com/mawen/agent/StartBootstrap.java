package com.mawen.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.mawen.agent.core.Bootstrap;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @see Main
 * @since 2024/3/8
 */
public class StartBootstrap {
	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

	private StartBootstrap() {
	}

	public static void premain(String agentArgs, Instrumentation inst, String javaAgentJarPath) {
		log.info("StartBootstrap classloader: {}", Thread.currentThread().getContextClassLoader().getClass());

		try {
			Bootstrap.start(agentArgs, inst, javaAgentJarPath);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
