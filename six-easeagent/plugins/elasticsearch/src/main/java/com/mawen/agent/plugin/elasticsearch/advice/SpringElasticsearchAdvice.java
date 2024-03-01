package com.mawen.agent.plugin.elasticsearch.advice;

import java.util.Set;

import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.utils.ClassUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class SpringElasticsearchAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return ClassMatcherUtils.name("");
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return null;
	}
}
