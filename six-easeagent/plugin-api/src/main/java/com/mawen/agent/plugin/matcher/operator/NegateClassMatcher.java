package com.mawen.agent.plugin.matcher.operator;

import com.mawen.agent.plugin.matcher.IClassMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class NegateClassMatcher implements IClassMatcher {
	protected IClassMatcher matcher;

	public NegateClassMatcher(IClassMatcher matcher) {
		this.matcher = matcher;
	}

	public IClassMatcher getMatcher() {
		return matcher;
	}
}
