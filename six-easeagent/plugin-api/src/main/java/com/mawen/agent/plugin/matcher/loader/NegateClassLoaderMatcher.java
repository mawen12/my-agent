package com.mawen.agent.plugin.matcher.loader;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class NegateClassLoaderMatcher implements IClassLoaderMatcher{
	IClassLoaderMatcher matcher;

	public NegateClassLoaderMatcher(IClassLoaderMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public String getClassLoaderName() {
		return matcher.getClassLoaderName();
	}

	@Override
	public IClassLoaderMatcher negate() {
		return this.matcher;
	}
}
