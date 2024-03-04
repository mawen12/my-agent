package com.mawen.agent.plugin.tools.matcher;

import com.mawen.agent.plugin.matcher.ClassMatcher;
import com.mawen.agent.plugin.matcher.IClassMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class ClassMatcherUtils {

	public static IClassMatcher name(String name) {
		return ClassMatcher.builder()
				.hasClassName(name)
				.build();
	}

	public static IClassMatcher hasSuperType(String name) {
		return ClassMatcher.builder()
				.hasSuperClass(name)
				.build();
	}
}
