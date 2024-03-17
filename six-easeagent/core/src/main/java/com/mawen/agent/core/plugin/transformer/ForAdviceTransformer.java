package com.mawen.agent.core.plugin.transformer;

import com.mawen.agent.core.plugin.CommonInlineAdvice;
import com.mawen.agent.core.plugin.annotation.Index;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.plugin.registry.AdviceRegistry;
import com.mawen.agent.core.plugin.transformer.advice.AgentAdvice;
import com.mawen.agent.core.plugin.transformer.advice.AgentForAdvice;
import com.mawen.agent.core.plugin.transformer.advice.AgentJavaConstantValue;
import com.mawen.agent.core.plugin.transformer.advice.MethodIdentityJavaConstant;
import com.mawen.agent.core.plugin.transformer.classloader.CompoundClassLoader;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ForAdviceTransformer implements AgentBuilder.Transformer {

	private final AgentForAdvice transformer;
	private final MethodTransformation methodTransformation;

	public ForAdviceTransformer(MethodTransformation methodTransformation) {
		this.methodTransformation = methodTransformation;

		var value = new MethodIdentityJavaConstant(methodTransformation.getIndex());
		var stackManipulation = new AgentJavaConstantValue(value, methodTransformation.getIndex());
		var typeDescription = value.getTypeDescription();

		var factory = new AgentAdvice.OffsetMapping.ForStackManipulation.Factory<>(Index.class,
				stackManipulation, typeDescription.asGenericType());

		var agentForAdvice = new AgentForAdvice(AgentAdvice.withCustomMapping().bind(factory));
		this.transformer = agentForAdvice
				.include(getClass().getClassLoader())
				.advice(methodTransformation.getMatcher(),
						CommonInlineAdvice.class.getCanonicalName());
	}

	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
		CompoundClassLoader.compound(this.getClass().getClassLoader(), classLoader);

		AdviceRegistry.setCurrentClassLoader(classLoader);
		var bd = transformer.transform(builder, typeDescription, classLoader, javaModule);
		AdviceRegistry.clearCurrentClassLoader();
		return bd;
	}
}
