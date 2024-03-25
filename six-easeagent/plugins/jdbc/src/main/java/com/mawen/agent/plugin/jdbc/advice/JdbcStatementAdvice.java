package com.mawen.agent.plugin.jdbc.advice;

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
public class JdbcStatementAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcher.builder()
				.hasInterface("java.sql.Statement")
				.notAbstract()
				.notInterface()
				.build();
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		IClassMatcher overriddenFrom = ClassMatcher.builder()
				.hasClassName("java.sql.Statement")
				.or()
				.hasClassName("java.sql.PreparedStatement")
				.build();

		return MethodMatcher.multiBuilder()
				.match(MethodMatcher.builder()
						.nameStartWith("execute")
						.isOverriddenFrom(overriddenFrom)
						.build())
				.match(MethodMatcher.builder()
						.named("addBatch")
						.or()
						.named("clearBatch")
						.qualifier("batch")
						.build())
				.build();
	}

	@Override
	public boolean isAddDynamicField() {
		return true;
	}
}
