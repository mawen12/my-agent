package com.mawen.agent.core.plugin.registry;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.mawen.agent.core.plugin.interceptor.ProviderChain.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
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
		String pointClassName = points.getClass().getCanonicalName();
		IClassMatcher classMatcher = points.getClassMatcher();
		boolean hasDynamicField = points.isAddDynamicField();
		ElementMatcher.Junction<TypeDescription> innerClassMatcher = ClassMatcherConvert.INSTANCE.convert(classMatcher);
		ElementMatcher<ClassLoader> loaderMatcher = ClassLoaderMatcherConvert.INSTANCE.convert(points.getClassLoaderMatcher());

		Set<IMethodMatcher> methodMatchers = points.getMethodMatcher();
		Set<MethodTransformation> mInfo = methodMatchers.stream().map(matcher -> {
			ElementMatcher.Junction<MethodDescription> bMethodMatcher = MethodMatcherConvert.INSTANCE.convert(matcher);
			String qualifier = getMethodQualifier(pointClassName, matcher.getQualifier());
			Integer index = QUALIFIER_TO_INDEX.get(qualifier);
			if (index == null) {
				return null;
			}
			Builder providerBuilder = INTERCEPTOR_PROVIDERS.get(index);
			MethodTransformation mt = new MethodTransformation(index, bMethodMatcher, providerBuilder);
			if (INDEX_TO_METHOD_TRANSFORMATION.putIfAbsent(index, mt) != null) {
				log.error("There are duplicate in Points: {}", qualifier);
			}
			return mt;
		}).filter(Objects::nonNull).collect(Collectors.toSet());

		AgentPlugin plugin = POINTS_TO_PLUGIN.get(pointClassName);
		int order = plugin.order();

		return ClassTransformation.builder()
				.classMatcher(innerClassMatcher)
				.hasDynamicField(hasDynamicField)
				.methodTransformations(mInfo)
				.classLoaderMatcher(loaderMatcher)
				.order(order)
				.build();
	}

	public static int register(InterceptorProvider provider) {
		String qualifier = provider.getAdviceTo();

		AgentPlugin plugin = PLUGIN_CLASSNAME_TO_PLUGIN.get(provider.getPluginClassName());
		if (plugin == null) {
			throw new RuntimeException();
		}

		QUALIFIER_TO_PLUGIN.putIfAbsent(qualifier, plugin);
		POINTS_TO_PLUGIN.putIfAbsent(getPointsClassName(qualifier), plugin);

		Integer index = QUALIFIER_TO_INDEX.get(provider.getAdviceTo());
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

	private PluginRegistry() {
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
