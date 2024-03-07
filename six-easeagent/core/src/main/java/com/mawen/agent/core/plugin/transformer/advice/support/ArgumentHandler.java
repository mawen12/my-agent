package com.mawen.agent.core.plugin.transformer.advice.support;

import java.util.List;
import java.util.SortedMap;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
public interface ArgumentHandler {
	int THIS_REFERENCE = 0;

	int argument(int offset);

	int exit();

	int enter();

	int named(String name);

	int returned();

	int thrown();

	interface ForInstrumentedMethod extends ArgumentHandler {
		int variable(int index);

		int prepare(MethodVisitor methodVisitor);

		ForAdvice bindEnter(MethodDescription adviceMethod);

		ForAdvice bindExit(MethodDescription adviceMethod, boolean skipThrowable);

		boolean isCopyingArguments();

		List<TypeDescription> getNamedTypes();

		@AllArgsConstructor
		abstract class Default implements ArgumentHandler.ForInstrumentedMethod {
			protected final MethodDescription method;
			protected final TypeDefinition exitType;
			protected final SortedMap<String, TypeDefinition> namedTypes;
			protected final TypeDefinition enterType;

			public int exit() {
				return method.getStackSize();
			}

			public int named(String name) {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.headMap(name).values());
			}

			public int enter() {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.values());
			}

			public int returned() {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.values())
						+ enterType.getStackSize().getSize();
			}

			public int thrown() {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.values())
						+ enterType.getStackSize().getSize()
						+ method.getReturnType().getStackSize().getSize();
			}

			public ForAdvice bindEnter(MethodDescription adviceMethod) {
				return new ForAdvice.Default.ForMethodEnter(method, adviceMethod, exitType, namedTypes);
			}

			public ForAdvice bindExit(MethodDescription adviceMethod, boolean skipThrowable) {
				return new ForAdvice.Default.ForMethodExit(method, adviceMethod, exitType, namedTypes, enterType,
						skipThrowable ? StackSize.ZERO : StackSize.SINGLE);
			}

			public List<TypeDescription> getNamedTypes() {
				return this.namedTypes.values().stream()
						.map(TypeDefinition::asErasure)
						.collect(Collectors.toList());
			}

			@HashCodeAndEqualsPlugin.Enhance
			protected static class Simple extends Default {
				public Simple(MethodDescription method, TypeDefinition exitType, SortedMap<String, TypeDefinition> namedTypes, TypeDefinition enterType) {
					super(method, exitType, namedTypes, enterType);
				}

				@Override
				public int variable(int index) {
					return index < (method.isStatic() ? 0 : 1) + method.getParameters().size()
							? index
							: index + (exitType.represents(void.class) ? 0 : 1) + namedTypes.size() + (enterType.represents(void.class) ? 0 : 1);
				}

				@Override
				public int prepare(MethodVisitor methodVisitor) {
					return 0;
				}

				@Override
				public boolean isCopyingArguments() {
					return false;
				}

				@Override
				public int argument(int offset) {
					return offset < method.getStackSize()
							? offset
							: offset + exitType.getStackSize().getSize() + StackSize.of(namedTypes.values()) + enterType.getStackSize().getSize();
				}
			}

			@HashCodeAndEqualsPlugin.Enhance
			protected static class Copying extends Default {

				public Copying(MethodDescription method, TypeDefinition exitType, SortedMap<String, TypeDefinition> namedTypes, TypeDefinition enterType) {
					super(method, exitType, namedTypes, enterType);
				}

				@Override
				public int variable(int index) {
					return (method.isStatic() ? 0 : 1)
							+ method.getParameters().size()
							+ (exitType.represents(void.class) ? 0 : 1)
							+ namedTypes.size()
							+ (enterType.represents(void.class) ? 0 : 1)
							+ index;
				}

				@Override
				public int prepare(MethodVisitor methodVisitor) {
					StackSize stackSize;
					if (!method.isStatic()) {
						methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
						methodVisitor.visitVarInsn(Opcodes.ASTORE, method.getStackSize()
								+ exitType.getStackSize().getSize()
								+ StackSize.of(namedTypes.values())
								+ enterType.getStackSize().getSize());
						stackSize = StackSize.SINGLE;
					}
					else {
						stackSize = StackSize.ZERO;
					}
					for (ParameterDescription parameter : method.getParameters()) {
						Type type = Type.getType(parameter.getType().asErasure().getDescriptor());
						methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ALOAD), parameter.getOffset());
						methodVisitor.visitVarInsn(type.getOpcode(Opcodes.ISTORE), method.getStackSize()
								+ exitType.getStackSize().getSize()
								+ StackSize.of(namedTypes.values())
								+ enterType.getStackSize().getSize()
								+ parameter.getOffset());
						stackSize = stackSize.maximum(parameter.getType().getStackSize());
					}
					return stackSize.getSize();
				}

				@Override
				public boolean isCopyingArguments() {
					return true;
				}

				@Override
				public int argument(int i) {
					return method.getStackSize()
							+ exitType.getStackSize().getSize()
							+ StackSize.of(namedTypes.values())
							+ enterType.getStackSize().getSize()
							+ i;
				}
			}
		}
	}

	interface ForAdvice extends ArgumentHandler, Advice.ArgumentHandler {
		int mapped(int offset);

		@AllArgsConstructor
		abstract class Default implements ArgumentHandler.ForAdvice {
			protected final MethodDescription method;
			protected final MethodDescription adviceMethod;
			protected final TypeDefinition exitType;
			protected final SortedMap<String, TypeDefinition> namedTypes;

			public int argument(int offset) {
				return offset;
			}

			public int exit() {
				return method.getStackSize();
			}

			public int named(String name) {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.headMap(name).values());
			}

			public int enter() {
				return method.getStackSize()
						+ exitType.getStackSize().getSize()
						+ StackSize.of(namedTypes.values());
			}

			@HashCodeAndEqualsPlugin.Enhance
			protected static class ForMethodEnter extends Default {

				public ForMethodEnter(MethodDescription method, MethodDescription adviceMethod, TypeDefinition exitType, SortedMap<String, TypeDefinition> namedTypes) {
					super(method, adviceMethod, exitType, namedTypes);
				}

				@Override
				public int returned() {
					throw new IllegalStateException("Cannot resolve the return value offset during enter advice");
				}

				@Override
				public int thrown() {
					throw new IllegalStateException("Cannot resolve the thrown value offset during enter advice");
				}

				@Override
				public int mapped(int offset) {
					return method.getStackSize()
							+ exitType.getStackSize().getSize()
							+ StackSize.of(namedTypes.values())
							+ adviceMethod.getStackSize()
							+ offset;
				}
			}

			@HashCodeAndEqualsPlugin.Enhance
			protected static class ForMethodExit extends Default {
				private final TypeDefinition enterType;
				private final StackSize throwableSize;

				public ForMethodExit(MethodDescription method, MethodDescription adviceMethod, TypeDefinition exitType, SortedMap<String, TypeDefinition> namedTypes,
						TypeDefinition enterType, StackSize throwableSize) {
					super(method, adviceMethod, exitType, namedTypes);
					this.enterType = enterType;
					this.throwableSize = throwableSize;
				}

				@Override
				public int returned() {
					return method.getStackSize()
							+ exitType.getStackSize().getSize()
							+ StackSize.of(namedTypes.values())
							+ enterType.getStackSize().getSize();
				}

				@Override
				public int thrown() {
					return method.getStackSize()
							+ exitType.getStackSize().getSize()
							+ StackSize.of(namedTypes.values())
							+ enterType.getStackSize().getSize()
							+ method.getReturnType().getStackSize().getSize();
				}

				@Override
				public int mapped(int offset) {
					return method.getStackSize()
							+ exitType.getStackSize().getSize()
							+ StackSize.of(namedTypes.values())
							+ enterType.getStackSize().getSize()
							+ method.getReturnType().getStackSize().getSize()
							+ throwableSize.getSize()
							+ adviceMethod.getStackSize()
							+ offset;
				}
			}
		}
	}

	enum Factory {
		SIMPLE {
			@Override
			protected ForInstrumentedMethod resolve(MethodDescription method, TypeDefinition enterType, TypeDescription exitType, SortedMap<String, TypeDefinition> namedTypes) {
				return new ForInstrumentedMethod.Default.Simple(method,
						exitType, namedTypes, enterType);
			}
		},
		COPYING {
			@Override
			protected ForInstrumentedMethod resolve(MethodDescription instrumentMethod, TypeDefinition enterType, TypeDescription exitType, SortedMap<String, TypeDefinition> namedTypes) {
				return new ForInstrumentedMethod.Default.Copying(instrumentMethod,
						exitType, namedTypes, enterType);
			}
		},
		;

		protected abstract ForInstrumentedMethod resolve(MethodDescription instrumentMethod,
				TypeDefinition enterType,
				TypeDescription exitType,
				SortedMap<String, TypeDefinition> namedTypes);
	}
}
