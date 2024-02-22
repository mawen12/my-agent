package com.mawen.agent.plugin.matcher.loader;

import com.mawen.agent.plugin.matcher.Matcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface IClassLoaderMatcher extends Matcher {

	String getClassLoaderName();

	IClassLoaderMatcher negate();

}
