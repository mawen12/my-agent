package com.mawen.agent.core;

import java.lang.instrument.Instrumentation;

import com.mawen.agent.config.ConfigFactory;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.mock.context.ContextManager;
import com.mawen.agent.plugin.api.config.ConfigConst;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class Bootstrap {

	private static final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
	private static final String AGENT_SERVER_PORT_KEY = ConfigFactory.AGENT_SERVER_PORT;
	private static final String AGENT_SERVER_ENABLED_KEY = ConfigFactory.AGENT_SERVER_ENABLED;
	private static final String AGENT_MIDDLEWARE_UPDATE = "agent.middleware.update";
	private static final int DEF_AGENT_SERVER_PORT = 9900;

	static final String MX_BEAN_OBJECT_NAME = "com.mawen.agent:type=ConfigManager";

	private static ContextManager contextManager;

	private Bootstrap(){}

	public static void start(String args, Instrumentation inst, String javaAgentJarPath) {
		long begin = System.nanoTime();
		System.setProperty(ConfigConst.AGENT_JAR_PATH, javaAgentJarPath);

		// add bootstrap classes

	}

}