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
public class JedisAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasSuperClass("redis.clients.jedis.Jedis")
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		IClassMatcher overriddenFrom = named("redis.clients.jedis.commands.JedisCommands")
				.or(named("redis.clients.jedis.JedisCommands"))
				.or(named("redis.clients.jedis.commands.AdvancedJedisCommands"))
				.or(named("redis.clients.jedis.commands.BasicCommands"))
				.or(named("redis.clients.jedis.commands.ClusterCommands"))
				.or(named("redis.clients.jedis.commands.ModuleCommands"))
				.or(named("redis.clients.jedis.commands.MultiKeyCommands"))
				.or(named("redis.clients.jedis.commands.ScriptingCommands"))
				.or(named("redis.clients.jedis.commands.SentinelCommands"))
				.or(named("redis.clients.jedis.commands.BinaryJedisCommands"))
				.or(named("redis.clients.jedis.commands.MultiKeyBinaryCommands"))
				.or(named("redis.clients.jedis.commands.AdvancedBinaryJedisCommands"))
				.or(named("redis.clients.jedis.commands.BinaryScriptingCommands"));

		return MethodMatcher.multiBuilder()
				.match(MethodMatcher.builder().isOverriddenFrom(overriddenFrom).build())
				.build();
	}

	private IClassMatcher named(String name) {
		return ClassMatcher.builder()
				.hasClassName(name)
				.isInterface()
				.build();
	}
}
