package com.mawen.agent.core.plugin.transformer;

import java.util.ArrayList;
import java.util.List;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class CompoundPluginTransformer implements AgentBuilder.Transformer {

	private final List<AgentBuilder.Transformer> transformers;

	public CompoundPluginTransformer(List<AgentBuilder.Transformer> transformers) {
		this.transformers = new ArrayList<>();
		for (var transformer : transformers) {
			if (transformer instanceof CompoundPluginTransformer compoundPluginTransformer) {
				this.transformers.addAll(compoundPluginTransformer.transformers);
				continue;
			}
			this.transformers.add(transformer);
		}
	}

	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
		for (var transformer : this.transformers) {
			builder = transformer.transform(builder, typeDescription, classLoader, javaModule);
		}
		return builder;
	}
}
