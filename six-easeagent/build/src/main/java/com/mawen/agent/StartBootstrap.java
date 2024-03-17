package com.mawen.agent;

import java.lang.instrument.Instrumentation;

import com.mawen.agent.core.Bootstrap;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/8
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StartBootstrap {
	private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

	public static void premain(String agentArgs, Instrumentation inst, String javaAgentJarPath) {
		log.info("StartBootstrap classloader: {}", Thread.currentThread().getContextClassLoader());
		Bootstrap.start(agentArgs, inst, javaAgentJarPath);
	}
}
