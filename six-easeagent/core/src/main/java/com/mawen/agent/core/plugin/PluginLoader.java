package com.mawen.agent.core.plugin;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mawen.agent.core.plugin.matcher.ClassTransformation;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.plugin.registry.PluginRegistry;
import com.mawen.agent.core.plugin.transformer.CompoundPluginTransformer;
import com.mawen.agent.core.plugin.transformer.DynamicFieldTransformer;
import com.mawen.agent.core.plugin.transformer.ForAdviceTransformer;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.field.AgentDynamicFieldAccessor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class PluginLoader {
	private static final Logger log = LoggerFactory.getLogger(PluginLoader.class);

	private PluginLoader() {
	}

	public static AgentBuilder load(AgentBuilder ab) {
		log.info("Loading plugins >>>>>");
		pluginLoad();
		providerLoad();
		var sortedTransformations = pointsLoad();

		for (var transformation : sortedTransformations) {
			ab = ab.type(transformation.getClassMatcher(), transformation.getClassLoaderMatcher())
					.transform(compound(transformation.isHasDynamicField(), transformation.getMethodTransformations()));
		}
		log.info("Loaded plugins <<<<<");
		return ab;
	}

	public static void pluginLoad() {
		for (var plugin : BaseLoader.loadOrdered(AgentPlugin.class)) {
			log.info("Loading plugin {}:{} [class {}]", plugin.getDomain(), plugin.getNamespace(), plugin.getClass().getName());

			try {
				PluginRegistry.register(plugin);
			}
			catch (Exception e) {
				log.error("Unable to load extension {}:{} [class {}]", plugin.getDomain(), plugin.getNamespace(), plugin.getClass().getName(), e);
			}
		}
	}

	public static void providerLoad() {
		for (var provider : BaseLoader.load(InterceptorProvider.class)) {
			log.info("loading provider: {}", provider.getClass().getName());

			try {
				log.debugIfEnabled("provider for:{} at {}", provider.getPluginClassName(), provider.getAdviceTo());
				PluginRegistry.register(provider);
			}
			catch (Exception | LinkageError e) {
				log.error("Unable to load provider in [class {}]", provider.getClass().getName(), e);
			}
		}
	}

	public static Set<ClassTransformation> pointsLoad() {
		var points = BaseLoader.load(Points.class);
		return points.stream()
				.map(point -> {
					try {
						return PluginRegistry.register(point);
					}
					catch (Exception e) {
						log.error("Unable to load points in [class {}]", point.getClass().getName(), e);
						return null;
					}
				}).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Ordered::order))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static AgentBuilder.Transformer compound(boolean hasDynamicField,
	                                                Iterable<MethodTransformation> transformers) {
		var agentTransformers = StreamSupport.stream(transformers.spliterator(), false)
				.map(ForAdviceTransformer::new)
				.collect(Collectors.<AgentBuilder.Transformer>toList());

		if (hasDynamicField) {
			agentTransformers.add(new DynamicFieldTransformer(AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME));
		}

		return new CompoundPluginTransformer(agentTransformers);
	}
}
