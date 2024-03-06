package com.mawen.agent.core.plugin.transformer;

import java.lang.annotation.Annotation;

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
import net.bytebuddy.implementation.bytecode.StackManipulation;
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

		MethodIdentityJavaConstant value = new MethodIdentityJavaConstant(methodTransformation.getIndex());
		StackManipulation stackManipulation = new AgentJavaConstantValue(value, methodTransformation.getIndex());
		TypeDescription typeDescription = value.getTypeDescription();

		AgentAdvice.OffsetMapping.Factory<Index> factory = new AgentAdvice.OffsetMapping.ForStackManipulation.Factory<>(Index.class,
				stackManipulation, typeDescription.asGenericType());

		this.transformer = new AgentForAdvice(AgentAdvice.withCustomMapping()
				.bind(factory))
				.include(getClass().getClassLoader())
				.advice(methodTransformation.getMatcher(),
						CommonInlineAdvice.class.getCanonicalName());
	}

	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
		CompoundClassLoader.compound(this.getClass().getClassLoader(), classLoader);

		AdviceRegistry.setCurrentClassLoader(classLoader);
		DynamicType.Builder<?> bd = transformer.transform(builder, typeDescription, classLoader, javaModule);
		AdviceRegistry.clearCurrentClassLoader();
		return bd;
	}
}
