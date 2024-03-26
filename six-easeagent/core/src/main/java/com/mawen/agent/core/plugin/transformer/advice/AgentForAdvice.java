package com.mawen.agent.core.plugin.transformer.advice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.LatentMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class AgentForAdvice extends AgentBuilder.Transformer.ForAdvice {

	private final AgentAdvice.WithCustomMapping advice;
	private final Advice.ExceptionHandler exceptionHandler;
	private final Assigner assigner;
	private final ClassFileLocator classFileLocator;
	private final AgentBuilder.PoolStrategy poolStrategy;
	private final AgentBuilder.LocationStrategy locationStrategy;
	private final List<Entry> entries;

	public AgentForAdvice(AgentAdvice.WithCustomMapping advice, Advice.ExceptionHandler exceptionHandler, Assigner assigner, ClassFileLocator classFileLocator, AgentBuilder.PoolStrategy poolStrategy, AgentBuilder.LocationStrategy locationStrategy, List<Entry> entries) {
		this.advice = advice;
		this.exceptionHandler = exceptionHandler;
		this.assigner = assigner;
		this.classFileLocator = classFileLocator;
		this.poolStrategy = poolStrategy;
		this.locationStrategy = locationStrategy;
		this.entries = entries;
	}

	public AgentForAdvice() {
		this(new AgentAdvice.WithCustomMapping());
	}

	public AgentForAdvice(AgentAdvice.WithCustomMapping advice) {
		this(advice,
				Advice.ExceptionHandler.Default.SUPPRESSING,
				Assigner.DEFAULT,
				ClassFileLocator.NoOp.INSTANCE,
				AgentBuilder.PoolStrategy.Default.FAST,
				AgentBuilder.LocationStrategy.ForClassLoader.STRONG,
				Collections.emptyList());
	}


	@Override
	public AgentForAdvice include(ClassLoader... classLoader) {
		LinkedHashSet<ClassFileLocator> classFileLocators = Arrays.stream(classLoader)
				.map(ClassFileLocator.ForClassLoader::of)
				.collect(Collectors.toCollection(LinkedHashSet::new));

		return include(new ArrayList<>(classFileLocators));
	}

	@Override
	public AgentForAdvice include(ClassFileLocator... classFileLocator) {
		return include(Lists.newArrayList(classFileLocator));
	}

	@Override
	public AgentForAdvice include(List<? extends ClassFileLocator> classFileLocators) {
		return new AgentForAdvice(advice,
				exceptionHandler,
				assigner,
				new ClassFileLocator.Compound(CompoundList.of(classFileLocators, classFileLocator)),
				poolStrategy,
				locationStrategy,
				entries);
	}

	@Override
	public AgentForAdvice advice(ElementMatcher<? super MethodDescription> matcher, String name) {
		return advice(new LatentMatcher.Resolved<>(matcher), name);
	}

	@Override
	public AgentForAdvice advice(LatentMatcher<? super MethodDescription> matcher, String name) {
		return new AgentForAdvice(advice,
				exceptionHandler,
				assigner,
				classFileLocator,
				poolStrategy,
				locationStrategy,
				CompoundList.of(entries, new ForUnifiedAdvice(matcher, name)));
	}

	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule module) {
		ClassFileLocator classFileLocator = new ClassFileLocator.Compound(this.classFileLocator, locationStrategy.classFileLocator(classLoader, module));
		TypePool typePool = poolStrategy.typePool(classFileLocator, classLoader);
		AsmVisitorWrapper.ForDeclaredMethods asmVisitorWrapper = new AsmVisitorWrapper.ForDeclaredMethods();

		for (Entry entry : entries) {
			asmVisitorWrapper = asmVisitorWrapper.invokable(
					entry.getMatcher().resolve(typeDescription),
					entry.resolve(advice, typePool, classFileLocator)
							.withAssigner(assigner)
							.withExceptionHandler(exceptionHandler)
			);
		}
		return builder.visit(asmVisitorWrapper);
	}

	@HashCodeAndEqualsPlugin.Enhance
	protected abstract static class Entry {
		private final LatentMatcher<? super MethodDescription> matcher;

		public Entry(LatentMatcher<? super MethodDescription> matcher) {
			this.matcher = matcher;
		}

		protected abstract AgentAdvice resolve(AgentAdvice.WithCustomMapping advice, TypePool typePool, ClassFileLocator classFileLocator);

		public LatentMatcher<? super MethodDescription> getMatcher() {
			return matcher;
		}
	}

	@HashCodeAndEqualsPlugin.Enhance
	protected static class ForUnifiedAdvice extends Entry {
		protected final String name;

		public ForUnifiedAdvice(LatentMatcher<? super MethodDescription> matcher, String name) {
			super(matcher);
			this.name = name;
		}

		@Override
		protected AgentAdvice resolve(AgentAdvice.WithCustomMapping advice, TypePool typePool, ClassFileLocator classFileLocator) {
			return advice.to(typePool.describe(name).resolve(), classFileLocator);
		}

		public String getName() {
			return name;
		}
	}
}
