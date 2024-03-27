package com.mawen.agent.plugin.redis.advice;

import java.util.Set;

import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.MethodMatcher;

import static com.mawen.agent.plugin.tools.matcher.ClassMatcherUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class JedisConstructorAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return name("redis.clients.jedis.Jedis");
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher.multiBuilder()
				.match(MethodMatcher.builder().named("<init>").qualifier("constructor").build())
				.build();
	}
}
