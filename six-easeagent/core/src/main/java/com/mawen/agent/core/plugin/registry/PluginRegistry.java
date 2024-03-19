package com.mawen.agent.core.plugin.registry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.google.common.base.Strings;
import com.mawen.agent.core.plugin.interceptor.ProviderPluginDecorator;
import com.mawen.agent.core.plugin.matcher.ClassLoaderMatcherConvert;
import com.mawen.agent.core.plugin.matcher.ClassMatcherConvert;
import com.mawen.agent.core.plugin.matcher.ClassTransformation;
import com.mawen.agent.core.plugin.matcher.MethodMatcherConvert;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.utils.AgentArray;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.mawen.agent.core.plugin.interceptor.ProviderChain.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginRegistry {
	private static final Logger log = Agent.getLogger(PluginRegistry.class);

	static final Map<String, AgentPlugin> QUALIFIER_TO_PLUGIN = new ConcurrentHashMap<>();
	static final Map<String, AgentPlugin> POINTS_TO_PLUGIN = new ConcurrentHashMap<>();
	static final Map<String, AgentPlugin> PLUGIN_CLASSNAME_TO_PLUGIN = new ConcurrentHashMap<>();
	static final Map<String, Integer> QUALIFIER_TO_INDEX = new ConcurrentHashMap<>();
	static final Map<Integer, MethodTransformation> INDEX_TO_METHOD_TRANSFORMATION = new ConcurrentHashMap<>();
	static final AgentArray<Builder> INTERCEPTOR_PROVIDERS = new AgentArray<>();

	public static void register(AgentPlugin plugin) {
		PLUGIN_CLASSNAME_TO_PLUGIN.putIfAbsent(plugin.getClass().getCanonicalName(), plugin);
	}

	private static String getMethodQualifier(String className, String qualifier) {
		return className.concat(":").concat(qualifier);
	}

	public static ClassTransformation register(Points points) {
		var pointClassName = points.getClass().getCanonicalName();
		var classMatcher = points.getClassMatcher();
		var hasDynamicField = points.isAddDynamicField();
		var innerClassMatcher = ClassMatcherConvert.INSTANCE.convert(classMatcher);
		var loaderMatcher = ClassLoaderMatcherConvert.INSTANCE.convert(points.getClassLoaderMatcher());

		var methodMatchers = points.getMethodMatcher();
		var mInfo = methodMatchers.stream().map(matcher -> {
			var bMethodMatcher = MethodMatcherConvert.INSTANCE.convert(matcher);
			var qualifier = getMethodQualifier(pointClassName, matcher.getQualifier());
			var index = QUALIFIER_TO_INDEX.get(qualifier);
			if (index == null) {
				return null;
			}
			var providerBuilder = INTERCEPTOR_PROVIDERS.get(index);
			var mt = new MethodTransformation(index, bMethodMatcher, providerBuilder);
			if (INDEX_TO_METHOD_TRANSFORMATION.putIfAbsent(index, mt) != null) {
				log.error("There are duplicate in Points: {}", qualifier);
			}
			return mt;
		}).filter(Objects::nonNull).collect(Collectors.toSet());

		var plugin = POINTS_TO_PLUGIN.get(pointClassName);
		var order = plugin.order();

		return ClassTransformation.builder()
				.classMatcher(innerClassMatcher)
				.hasDynamicField(hasDynamicField)
				.methodTransformations(mInfo)
				.classLoaderMatcher(loaderMatcher)
				.order(order)
				.build();
	}

	public static int register(InterceptorProvider provider) {
		var qualifier = provider.getAdviceTo();

		var plugin = PLUGIN_CLASSNAME_TO_PLUGIN.get(provider.getPluginClassName());
		if (plugin == null) {
			throw new RuntimeException();
		}

		QUALIFIER_TO_PLUGIN.putIfAbsent(qualifier, plugin);
		POINTS_TO_PLUGIN.putIfAbsent(getPointsClassName(qualifier), plugin);

		var index = QUALIFIER_TO_INDEX.get(provider.getAdviceTo());
		if (index == null) {
			synchronized (QUALIFIER_TO_INDEX) {
				index = QUALIFIER_TO_INDEX.get(provider.getAdviceTo());
				if (index == null) {
					index = INTERCEPTOR_PROVIDERS.add(builder());
					QUALIFIER_TO_INDEX.putIfAbsent(provider.getAdviceTo(), index);
				}
			}
		}
		INTERCEPTOR_PROVIDERS.get(index)
				.addProvider(new ProviderPluginDecorator(plugin, provider));

		return index;
	}

	static String getPointsClassName(String name) {
		int index;
		if (Strings.isNullOrEmpty(name)) {
			return "unknown";
		}
		index = name.lastIndexOf(':');
		if (index < 0) {
			return name;
		}
		return name.substring(0, index);
	}

	public static MethodTransformation getMethodTransformation(int pointcutIndex) {
		return INDEX_TO_METHOD_TRANSFORMATION.get(pointcutIndex);
	}

	public static void addMethodTransformation(int pointcutIndex, MethodTransformation transformation) {
		INDEX_TO_METHOD_TRANSFORMATION.putIfAbsent(pointcutIndex, transformation);
	}
}
