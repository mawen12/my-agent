package com.mawen.agent.core.plugin.transformer.advice;

import java.util.Map;

import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.utility.OpenedClassReader;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class BypassMethodVisitor extends MethodVisitor {

	public BypassMethodVisitor(MethodVisitor visitor, Map<Integer, AgentAdvice.OffsetMapping> offsetMappings) {
		super(OpenedClassReader.ASM_API, visitor);
	}
}
