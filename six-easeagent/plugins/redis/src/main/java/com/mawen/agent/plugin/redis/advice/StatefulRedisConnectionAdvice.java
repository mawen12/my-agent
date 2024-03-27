package com.mawen.agent.plugin.redis.advice;

import java.util.Set;

import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.matcher.ClassMatcher;
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.MethodMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class StatefulRedisConnectionAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasSuperClass("io.lettuce.core.api.StatefulConnection")
				.notInterface()
				.notAbstract()
				.or()
				.hasSuperClass("io.lettuce.core.sentinel.api.StatefulRedisSentinelConnection")
				.notInterface()
				.notAbstract()
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.multiBuilder()
				.match(MethodMatcher.builder().named("<init>").qualifier("constructor").build())
				.build();
	}
}
