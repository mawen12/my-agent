package com.mawen.agent.core.plugin;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.mawen.agent.config.Configs;
import com.mawen.agent.core.plugin.matcher.ClassTransformation;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.plugin.registry.PluginRegistry;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.AgentPlugin;
import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.Points;
import com.mawen.agent.plugin.field.AgentDynamicFieldAccessor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.bytebuddy.agent.builder.AgentBuilder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PluginLoader {

	private static Logger log = LoggerFactory.getLogger(PluginLoader.class);

	public static AgentBuilder load(AgentBuilder ab, Configs conf) {
		pluginLoad();
		providerLoad();
		Set<ClassTransformation> sortedTransformations = pointsLoad();

		for (ClassTransformation transformation : sortedTransformations) {
			ab = ab.type(transformation.getClassMatcher(), transformation.getClassLoaderMatcher())
					.transform(compound())
		}
	}

	public static void providerLoad() {
		for (InterceptorProvider provider : BaseLoader.load(InterceptorProvider.class)) {
			log.debug("loading provider: {}", provider.getClass().getName());

			try {
				log.debug("provider for:{} at {}",
						provider.getPluginClassName(),
						provider.getAdviceTo());
				PluginRegistry.register(provider);
			}
			catch (Exception | LinkageError e) {
				log.error("Unable to load provider in [class []]",
						provider.getClass().getName(), e);
			}
		}
	}

	public static Set<ClassTransformation> pointsLoad() {
		List<Points> points = BaseLoader.load(Points.class);
		return points.stream()
				.map(point -> {
					try {
						return PluginRegistry.register(point);
					}
					catch (Exception e) {
						log.error("Unable to load points in [class []]",
								point.getClass().getName(),
								e);
						return null;
					}
				}).filter(Objects::nonNull)
				.sorted(Comparator.comparing(Ordered::order))
				.collect(Collectors.toCollection(LinkedHashSet::new));
	}

	public static void pluginLoad() {
		for (AgentPlugin plugin : BaseLoader.loadOrdered(AgentPlugin.class)) {
			log.info("Loading plugin {}:{} [class {}]",
					plugin.getDomain(),
					plugin.getNamespace(),
					plugin.getClass().getName());

			try {
				PluginRegistry.register(plugin);
			}
			catch (Exception e) {
				log.error("Unable to load extension {}:{} [class {}]",
						plugin.getDomain(),
						plugin.getNamespace(),
						plugin.getClass().getName(),
						e
				);
			}
		}
	}

	public static AgentBuilder.Transformer compound(boolean hasDynamicField,
			Iterable<MethodTransformation> transformers) {
		List<AgentBuilder.Transformer> agentTransformers = StreamSupport.stream(transformers.spliterator(), false)
				.map(ForAdviceTransformer::new)
				.collect(Collectors.toList());

		if (hasDynamicField) {
			agentTransformers.add(new DynamicFieldTransformer(AgentDynamicFieldAccessor.DYNAMIC_FIELD_NAME));
		}

		return new CompoundPluginTransformer(agentTransformers);
	}
}