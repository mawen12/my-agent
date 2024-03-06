package com.mawen.agent.core.plugin.transformer.advice;

import java.util.List;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.LatentMatcher;
import net.bytebuddy.pool.TypePool;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class AgentForAdvice extends AgentBuilder.Transformer.ForAdvice {

	private final Advice.WithCustomMapping advice;
	private final Advice.ExceptionHandler exceptionHandler;
	private final Assigner assigner;
	private final ClassFileLocator classFileLocator;
	private final AgentBuilder.PoolStrategy poolStrategy;
	private final AgentBuilder.LocationStrategy locationStrategy;
//	private final List<Entry>

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	@HashCodeAndEqualsPlugin.Enhance
	protected abstract static class Entry {
		private final LatentMatcher<? super MethodDescription> matcher;

		protected abstract AgentAdvice resolve(AgentAdvice.WithCustomMapping advice,
				TypePool typePool, ClassFileLocator classFileLocator);
	}

	@HashCodeAndEqualsPlugin.Enhance
	protected static class ForUnifiedAdvice extends Entry {
		protected final String name;

		protected ForUnifiedAdvice(LatentMatcher<? super MethodDescription> matcher, String name) {
			super(matcher);
			this.name = name;
		}

		@Override
		protected AgentAdvice resolve(AgentAdvice.WithCustomMapping advice, TypePool typePool, ClassFileLocator classFileLocator) {
			return advice.to(typePool.describe(name).resolve(), classFileLocator);
		}
	}
}
