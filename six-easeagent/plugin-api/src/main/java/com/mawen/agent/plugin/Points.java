package com.mawen.agent.plugin;

import java.util.Set;

import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.loader.ClassLoaderMatcher;
import com.mawen.agent.plugin.matcher.loader.IClassLoaderMatcher;

/**
 * Pointcut can be defined by ProbeDefine implementation
 * and also can be defined through @OnClass and @OnMethod annotation
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface Points {

	/**
	 * return the defined class matcher matching a class or a group of classes
	 *
	 * <p>
	 * eg.
	 * <pre>{@code
	 * 	ClassMatcher.builder()
	 * 		.hasInterface(A)
	 * 		.isPublic()
	 * 		.isAbstract()
	 * 		.or()
	 * 			.hasSuperClass(B)
	 * 			.isPublic()
	 * 			.build();
	 * }</pre>
	 * </p>
	 */
	IClassMatcher getClassMatcher();

	/**
	 * return the defined method matcher
	 *
	 * <p>
	 * eg.
	 * <pre>{@code
	 * 	MethodMatcher.builder().named("execute")
	 * 		.isPublic()
	 * 		.argNum(2)
	 * 		.arg(1, "java.lang.String")
	 * 		.build().toSet();
	 * 	or
	 * 	MethodMatcher.multiBuilder()
	 * 		.match(MethodMatcher.builder().named("<init>")
	 * 			.argsLength(3)
	 * 			.arg(0, "org.apache.kafka.clients.consumer.ConsumerConfig")
	 * 			.qualifier("constructor")
	 * 			.build())
	 * 		.match(MethodMatcher.builder().named("poll")
	 * 			.argsLength(1)
	 * 			.arg(0, "java.time.Duration")
	 * 			.qualifier("poll")
	 * 			.build())
	 * 		.build();
	 * }</pre>
	 * </p>
	 */
	Set<IMethodMatcher> getMethodMatcher();

	/**
	 * when return true, the transformer will add a Object field and a accessor.
	 * The dynamically added member can be accessed by AgentDynamicFieldAccessor:
	 *
	 * <pre>{@code
	 * 	AgentDynamicFieldAccessor.setDynamicFieldValue(instance, value);
	 * 	value = AgentDynamicFieldAccessor.getDynamicFieldValue(instance);
	 * }</pre>
	 */
	default boolean isAddDynamicField() {
		return false;
	}

	/**
	 * Only match classes loaded by the ClassLoaderMatcher
	 * default as all classloader
	 *
	 * @return classloader matcher
	 */
	default IClassLoaderMatcher getClassLoaderMatcher() {
		return ClassLoaderMatcher.ALL;
	}
}
