package com.mawen.agent.plugin.matcher.operator;

import com.mawen.agent.plugin.matcher.IMethodMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class AndMethodMatcher implements IMethodMatcher {
	private String qualifier = DEFAULT_QUALIFIER;
	protected IMethodMatcher left;
	protected IMethodMatcher right;

	public AndMethodMatcher(IMethodMatcher left, IMethodMatcher right) {
		this.left = left;
		this.right = right;

		this.qualifier(left.getQualifier());
		this.qualifier(right.getQualifier());
	}

	public IMethodMatcher qualifier(String q) {
		if (this.qualifier.equals(DEFAULT_QUALIFIER)) {
			this.qualifier = q;
		}

		return this;
	}

	@Override
	public String getQualifier() {
		return this.qualifier;
	}

	public IMethodMatcher getLeft() {
		return left;
	}

	public IMethodMatcher getRight() {
		return right;
	}
}
