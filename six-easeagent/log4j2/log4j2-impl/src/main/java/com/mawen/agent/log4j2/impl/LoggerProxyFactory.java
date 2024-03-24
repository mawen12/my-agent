package com.mawen.agent.log4j2.impl;

import com.mawen.agent.log4j2.api.AgentLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.LoggingException;
import org.apache.logging.log4j.spi.AbstractLoggerAdapter;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.slf4j.Logger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class LoggerProxyFactory extends AbstractLoggerAdapter<AgentLoggerProxy> {

	private static final LoggerProxyFactory INSTANCE = new LoggerProxyFactory();

	private static final String FQCN = LoggerProxyFactory.class.getName();
	private static final String PACKAGE = "org.slf4j";
	private static final String TO_SLF4J_CONTEXT = "org.apache.logging.slf4j.SLF4JLoggerContext";

	private final String loggerFqcn;

	public LoggerProxyFactory() {
		this(AgentLogger.class.getName());
	}

	public LoggerProxyFactory(String loggerFqcn) {
		this.loggerFqcn = loggerFqcn;
	}

	public static Slf4jLogger getAgentLogger(String name) {
		return new Slf4jLogger(INSTANCE.getLogger(name));
	}

	@Override
	protected AgentLoggerProxy newLogger(String name, LoggerContext context) {
		final var key = Logger.ROOT_LOGGER_NAME.equals(name) ? LogManager.ROOT_LOGGER_NAME : name;
		return new AgentLoggerProxy(validateContext(context).getLogger(key), name, loggerFqcn);
	}

	@Override
	protected LoggerContext getContext() {
		final var anchor = StackLocatorUtil.getCallerClass(FQCN, PACKAGE);
		return (anchor == null) ? LogManager.getContext() : getContext(StackLocatorUtil.getCallerClass(anchor));
	}

	private LoggerContext validateContext(final LoggerContext context) {
		if (TO_SLF4J_CONTEXT.equals(context.getClass().getName())) {
			throw new LoggingException("log4j-slf4j-impl cannot be present with log4j-to-slf4j");
		}
		return context;
	}
}
