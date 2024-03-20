package com.mawen.agent.zipkin.logging;

import brave.baggage.CorrelationScopeDecorator;
import brave.internal.CorrelationContext;
import brave.propagation.CurrentTraceContext;
import com.mawen.agent.plugin.bridge.Agent;
import org.slf4j.MDC;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class AgentMDCScopeDecorator {

	static final CurrentTraceContext.ScopeDecorator INSTANCE = new BuilderApp().build();
	static final CurrentTraceContext.ScopeDecorator INSTANCE_V2 = new BuilderEaseLogger().build();
	static final CurrentTraceContext.ScopeDecorator INSTANCE_AGENT_LOADER = new BuilderAgentLoader().build();

	public static CurrentTraceContext.ScopeDecorator get() {
		return INSTANCE;
	}

	public static CurrentTraceContext.ScopeDecorator getV2() {
		return INSTANCE_V2;
	}

	public static CurrentTraceContext.ScopeDecorator getAgentDecorator() {
		return INSTANCE_AGENT_LOADER;
	}

	static final class BuilderApp extends CorrelationScopeDecorator.Builder {
		BuilderApp() {
			super(MDCContextApp.INSTANCE);
		}
	}

	static final class BuilderEaseLogger extends CorrelationScopeDecorator.Builder {
		BuilderEaseLogger() {
			super(MDCContextEaseLogger.INSTANCE);
		}
	}

	static final class BuilderAgentLoader extends CorrelationScopeDecorator.Builder {
		BuilderAgentLoader() {
			super(MDCContextAgentLoader.INSTANCE);
		}
	}

	enum MDCContextApp implements CorrelationContext {
		INSTANCE;

		@Override
		public String getValue(String name) {
			ClassLoader classLoader =  getUserClassLoader();
			AgentLogMDC agentLogMDC = AgentLogMDC.create(classLoader);
			return agentLogMDC != null ? agentLogMDC.get(name) : null;
		}

		@Override
		public boolean update(String name, String value) {
			ClassLoader cLassLoader = getUserClassLoader();
			AgentLogMDC agentLogMDC = AgentLogMDC.create(cLassLoader);
			if (agentLogMDC == null) {
				return true;
			}
			if (value != null) {
				agentLogMDC.put(name, value);
			} else {
				agentLogMDC.remove(name);
			}
			return true;
		}

		private ClassLoader getUserClassLoader() {
			return Thread.currentThread().getContextClassLoader();
		}
	}

	enum MDCContextEaseLogger implements CorrelationContext {
		INSTANCE;

		@Override
		public String getValue(String name) {
			return Agent.loggerMdc.get(name);
		}

		@Override
		public boolean update(String name, String value) {
			if (value != null) {
				Agent.loggerMdc.put(name, value);
			} else {
				Agent.loggerMdc.remove(name);
			}
			return true;
		}
	}

	enum MDCContextAgentLoader implements CorrelationContext {
		INSTANCE;


		@Override
		public String getValue(String name) {
			return MDC.get(name);
		}

		@Override
		public boolean update(String name, String value) {
			if (value != null) {
				MDC.put(name, value);
			} else {
				MDC.remove(name);
			}
			return true;
		}
	}

}
