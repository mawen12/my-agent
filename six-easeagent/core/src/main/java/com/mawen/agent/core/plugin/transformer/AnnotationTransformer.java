package com.mawen.agent.core.plugin.transformer;

import com.mawen.agent.core.plugin.annotation.AgentInstrumented;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.AsmVisitorWrapper;
import net.bytebuddy.asm.MemberAttributeExtension;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.pool.TypePool;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class AnnotationTransformer implements AgentBuilder.Transformer {
	private final AsmVisitorWrapper visitorWrapper;
	private final AnnotationDescription annotationDescription;

	public AnnotationTransformer(MethodTransformation methodTransformation) {
		this.annotationDescription = AnnotationDescription.Builder
				.ofType(AgentInstrumented.class)
				.define("value", methodTransformation.index())
				.build();
		var forMethod = new MemberAttributeExtension.ForMethod()
				.annotateMethod(this.annotationDescription);
		this.visitorWrapper = new ForMethodDelegate(annotationDescription.getAnnotationType(), forMethod, methodTransformation)
				.on(methodTransformation.matcher());
	}


	@Override
	public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
		return builder.visit(this.visitorWrapper);
	}

	public static record ForMethodDelegate(TypeDescription annotation, MemberAttributeExtension.ForMethod forMethod, MethodTransformation methodTransformation)
			implements AsmVisitorWrapper.ForDeclaredMethods.MethodVisitorWrapper {

		@Override
		public MethodVisitor wrap(TypeDescription typeDescription, MethodDescription methodDescription, MethodVisitor methodVisitor, Implementation.Context context, TypePool typePool, int i, int i1) {
			AnnotationDescription annotation = methodDescription.getDeclaredAnnotations().ofType(this.annotation);
			if (annotation != null) {
				annotation.getValue("value").resolve(Integer.class);
			}
			return methodVisitor;
		}

		public AsmVisitorWrapper on(ElementMatcher<? super MethodDescription> matcher) {
			return new AsmVisitorWrapper.ForDeclaredMethods().invokable(matcher, this);
		}
	}
}
