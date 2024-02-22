package com.mawen.agent.plugin.matcher;

import java.util.HashSet;
import java.util.Set;

import com.mawen.agent.plugin.matcher.operator.AndMethodMatcher;
import com.mawen.agent.plugin.matcher.operator.NegateMethodMatcher;
import com.mawen.agent.plugin.matcher.operator.Operator;
import com.mawen.agent.plugin.matcher.operator.OrMethodMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface IMethodMatcher extends Operator<IMethodMatcher>, Matcher {
	String DEFAULT_QUALIFIER = "default";

	String getQualifier();

	default boolean isDefaultQualifier() {
		return this.getQualifier().equals(DEFAULT_QUALIFIER);
	}

	default Set<IMethodMatcher> toSet() {
		Set<IMethodMatcher> set = new HashSet<>();
		set.add(this);
		return set;
	}

	default IMethodMatcher and(IMethodMatcher other) {
		return new AndMethodMatcher(this, other);
	}

	default IMethodMatcher or(IMethodMatcher other) {
		return new OrMethodMatcher(this, other);
	}

	default IMethodMatcher negate() {
		return new NegateMethodMatcher(this);
	}
}
