package com.mawen.agent.core.plugin.transformer.advice;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.jar.asm.MethodVisitor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@Getter
@SuperBuilder
public class AgentJavaConstantValue extends JavaConstantValue {

	private final MethodIdentityJavaConstant constant;
	private final int pointcutIndex;

	@Override
	public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
		var index = (Integer) constant.accept(Visitor.INSTANCE);
		methodVisitor.visitLdcInsn(index);
		return constant.getTypeDescription().getStackSize().toIncreasingSize();
	}
}
