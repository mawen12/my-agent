package com.mawen.agent.plugin.matcher.operator;

import com.mawen.agent.plugin.matcher.IMethodMatcher;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
@Getter
public class NegateMethodMatcher implements IMethodMatcher {
	private String qualifier = DEFAULT_QUALIFIER;
	protected IMethodMatcher matcher;

	public NegateMethodMatcher(IMethodMatcher matcher) {
		this.matcher = matcher;
		this.qualifier(matcher.getQualifier());
	}

	public IMethodMatcher qualifier(String q) {
		this.qualifier = q;
		return this;
	}
}
