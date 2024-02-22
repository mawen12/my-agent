package com.mawen.agent.plugin.matcher;

import com.mawen.agent.plugin.matcher.operator.AndClassMatcher;
import com.mawen.agent.plugin.matcher.operator.NegateClassMatcher;
import com.mawen.agent.plugin.matcher.operator.Operator;
import com.mawen.agent.plugin.matcher.operator.OrClassMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface IClassMatcher extends Operator<IClassMatcher>, Matcher {

	default IClassMatcher and(IClassMatcher m) {
		return new AndClassMatcher(this, m);
	}

	default IClassMatcher or(IClassMatcher m) {
		return new OrClassMatcher(this, m);
	}

	default IClassMatcher negate() {
		return new NegateClassMatcher(this);
	}
}