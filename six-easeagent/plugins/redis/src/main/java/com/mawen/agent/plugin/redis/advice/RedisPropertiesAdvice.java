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
public class RedisPropertiesAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasClassName("org.springframework.boot.autoconfigure.data.redis.RedisProperties")
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.multiBuilder()
				.match(MethodMatcher.builder().nameStartWith("set").build())
				.build();
	}
}
