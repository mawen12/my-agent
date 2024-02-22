package com.mawen.agent.plugin.matcher.operator;

import com.mawen.agent.plugin.matcher.IClassMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class OrClassMatcher implements IClassMatcher {
	protected IClassMatcher left;
	protected IClassMatcher right;

	public OrClassMatcher(IClassMatcher left, IClassMatcher right) {
		this.left = left;
		this.right = right;
	}

	public IClassMatcher getLeft() {
		return left;
	}

	public IClassMatcher getRight() {
		return right;
	}
}
