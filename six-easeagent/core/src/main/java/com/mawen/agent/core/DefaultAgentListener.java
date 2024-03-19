package com.mawen.agent.core;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 *
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/18
 */
public enum DefaultAgentListener implements AgentBuilder.Listener {
	INSTANCE;

	private static final Logger log = LoggerFactory.getLogger(DefaultAgentListener.class);

	@Override
	public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
		// ignored
	}

	@Override
	public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {
		log.info("onTransformation: {} loaded: {} from classloader {}", typeDescription, b, classLoader);
	}

	@Override
	public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {
		// ignored
	}

	@Override
	public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {
		log.warn("Just for Debug-log, transform ends exceptionally, which is sometimes normal and sometimes there is an error: {} error:{} loaded: {} from classLoader {}",
				s, throwable, b, classLoader);
	}

	@Override
	public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {
		// ignored
	}
}
