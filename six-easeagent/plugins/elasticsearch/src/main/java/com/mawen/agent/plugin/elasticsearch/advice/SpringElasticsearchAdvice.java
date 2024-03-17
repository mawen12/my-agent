package com.mawen.agent.plugin.elasticsearch.advice;

import java.util.Set;

import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.MethodMatcher;

import static com.mawen.agent.plugin.tools.matcher.ClassMatcherUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
public class SpringElasticsearchAdvice implements Points {

	@Override
	public IClassMatcher getClassMatcher() {
		return name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties")
				.or(hasSuperType("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"))
				.or(name("org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientProperties"))
				.or(name("org.springframework.boot.autoconfigure.data.elasticsearch.ReactiveElasticsearchRestClientProperties"));
	}

	@Override
	public Set<IMethodMatcher> getMethodMatcher() {
		return MethodMatcher
				.multiBuilder()
				.matcher(MethodMatcher.builder().nameStartWith("set").build())
				.build();
	}
}
