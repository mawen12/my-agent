package com.mawen.agent.core.plugin.transformer.advice.support;

import java.util.Collections;
import java.util.List;

import com.mawen.agent.core.utils.AdviceUtils;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassWriter;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.Type;
import net.bytebuddy.utility.CompoundList;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
public interface StackMapFrameHandler {

	void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack);

	void injectReturnFrame(MethodVisitor methodVisitor);

	void injectExceptionFrame(MethodVisitor methodVisitor);

	void injectCompletionFrame(MethodVisitor methodVisitor);

	interface ForPostProcessor {
		void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack);
	}

	interface ForInstrumentedMethod extends StackMapFrameHandler {
		ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod);

		ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod);

		int getReaderHint();

		void injectInitializationFrame(MethodVisitor methodVisitor);

		void injectStartFrame(MethodVisitor methodVisitor);

		void injectPostCompletionFrame(MethodVisitor methodVisitor);
	}

	interface ForAdvice extends StackMapFrameHandler, ForPostProcessor, Advice.StackMapFrameHandler.ForPostProcessor {
		// marker interface
	}

	enum NoOp implements ForInstrumentedMethod, ForAdvice {
		INSTANCE;

		@Override
		public StackMapFrameHandler.ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
			return this;
		}

		@Override
		public StackMapFrameHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
			return this;
		}

		@Override
		public void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
			// ignored
		}

		@Override
		public void injectReturnFrame(MethodVisitor methodVisitor) {
			// ignored
		}

		@Override
		public void injectExceptionFrame(MethodVisitor methodVisitor) {
			// ignored
		}

		@Override
		public void injectCompletionFrame(MethodVisitor methodVisitor) {
			// ignored
		}

		@Override
		public void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack) {
			// ignored
		}

		@Override
		public int getReaderHint() {
			return ClassReader.SKIP_FRAMES;
		}

		@Override
		public void injectInitializationFrame(MethodVisitor methodVisitor) {
			// ignored
		}

		@Override
		public void injectStartFrame(MethodVisitor methodVisitor) {
			// ignored
		}

		@Override
		public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
			// ignored
		}
	}

	@SuperBuilder
	@AllArgsConstructor
	abstract class Default implements ForInstrumentedMethod {
		protected static final Object[] EMPTY = new Object[0];

		protected final TypeDescription type;
		protected final MethodDescription method;
		protected final List<? extends TypeDescription> initialTypes;
		protected final List<? extends TypeDescription> latentTypes;
		protected final List<? extends TypeDescription> preMethodTypes;
		protected final List<? extends TypeDescription> postMethodTypes;
		protected final boolean expandFrames;
		protected int currentFrameDivergence;

		public static ForInstrumentedMethod of(TypeDescription type,
		                                       MethodDescription method,
		                                       List<? extends TypeDescription> initialTypes,
		                                       List<? extends TypeDescription> latentTypes,
		                                       List<? extends TypeDescription> preMethodTypes,
		                                       List<? extends TypeDescription> postMethodTypes,
		                                       boolean exitAdvice,
		                                       boolean copyArguments,
		                                       ClassFileVersion classFileVersion,
		                                       int writeFlags,
		                                       int readerFlags) {
			if ((writeFlags & ClassWriter.COMPUTE_FRAMES) != 0
					|| classFileVersion.isLessThan(ClassFileVersion.JAVA_V6)) {
				return NoOp.INSTANCE;
			}
			else if (!exitAdvice && initialTypes.isEmpty()) {
				return new Trivial(type, method, latentTypes, (readerFlags & ClassReader.EXPAND_FRAMES) != 0);
			}
			else if (copyArguments) {
				return new WithPreservedArguments.WithArgumentCopy(type,
						method,
						initialTypes,
						latentTypes,
						preMethodTypes,
						postMethodTypes,
						(readerFlags & ClassReader.EXPAND_FRAMES) != 0);
			}
			else {
				return new WithPreservedArguments.WithoutArgumentCopy(type,
						method,
						initialTypes,
						latentTypes,
						preMethodTypes,
						postMethodTypes,
						(readerFlags & ClassReader.EXPAND_FRAMES) != 0,
						!method.isConstructor());
			}
		}

		@Override
		public ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
			return new Default.ForAdvice(adviceMethod, initialTypes, latentTypes, preMethodTypes, TranslationMode.ENTER,
					method.isConstructor() ? Initialization.UNITIALIZED : Initialization.INITIALIZED);
		}

		@Override
		public int getReaderHint() {
			return 0;
		}

		protected void translateFrame(MethodVisitor methodVisitor,
		                              TranslationMode translationMode,
		                              MethodDescription methodDescription,
		                              List<? extends TypeDescription> additionalTypes,
		                              int type,
		                              int localVariableLength,
		                              Object[] localVariable,
		                              int stackSize,
		                              Object[] stack) {

		}

		protected void injectFullFrame(MethodVisitor methodVisitor,
		                               Initialization initialization,
		                               List<? extends TypeDescription> typesInArray,
		                               List<? extends TypeDescription> typesOnStack) {

		}

		protected enum TranslationMode {
			COPY {
				@Override
				protected int copy(TypeDescription type, MethodDescription instrumentedMethod, MethodDescription methodDescription, Object[] localVariable, Object[] translated) {
					int length = instrumentedMethod.getParameters().size()
							+ (instrumentedMethod.isStatic() ? 0 : 1);
					System.arraycopy(localVariable, 0, translated, 0, length);
					return length;
				}

				@Override
				protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
					return instrumentedMethod.isConstructor()
							&& Opcodes.UNINITIALIZED_THIS.equals(frame)
							|| Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);

				}
			},

			ENTER {
				@Override
				protected int copy(TypeDescription type, MethodDescription instrumentedMethod, MethodDescription methodDescription, Object[] localVariable, Object[] translated) {
					int index = 0;
					if (!instrumentedMethod.isStatic()) {
						translated[index++] = instrumentedMethod.isConstructor()
								? Opcodes.UNINITIALIZED_THIS
								: Initialization.INITIALIZED.toFrame(type);
					}
					for (TypeDescription typeDefinition : instrumentedMethod.getParameters().asTypeList().asErasures()) {
						translated[index++] = Initialization.INITIALIZED.toFrame(typeDefinition);
					}
					return index;
				}

				@Override
				protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
					return instrumentedMethod.isConstructor()
							? Opcodes.UNINITIALIZED_THIS.equals(frame)
							: Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);
				}
			},

			EXIT {
				@Override
				protected int copy(TypeDescription type, MethodDescription instrumentedMethod, MethodDescription methodDescription, Object[] localVariable, Object[] translated) {
					int index = 0;
					if (!instrumentedMethod.isStatic()) {
						translated[index++] = Initialization.INITIALIZED.toFrame(type);
					}
					for (var typeDescription : instrumentedMethod.getParameters().asTypeList().asErasures()) {
						translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
					}
					return index;
				}

				@Override
				protected boolean isPossibleThisFrameValue(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Object frame) {
					return Initialization.INITIALIZED.toFrame(instrumentedType).equals(frame);
				}
			},
			;

			protected abstract int copy(TypeDescription type,
			                            MethodDescription instrumentedMethod,
			                            MethodDescription methodDescription,
			                            Object[] localVariable,
			                            Object[] translated);

			protected abstract boolean isPossibleThisFrameValue(TypeDescription instrumentedType,
			                                                    MethodDescription instrumentedMethod, Object frame);
		}

		protected enum Initialization {
			UNITIALIZED {
				@Override
				protected Object toFrame(TypeDescription typeDescription) {
					if (typeDescription.isPrimitive()) {
						throw new IllegalArgumentException("Cannot assume primitive uninitialized value: " + typeDescription);
					}
					return Opcodes.UNINITIALIZED_THIS;
				}
			},
			INITIALIZED {
				@Override
				protected Object toFrame(TypeDescription typeDescription) {
					return AdviceUtils.getFrameElementType(typeDescription);
				}
			},
			;

			protected abstract Object toFrame(TypeDescription typeDescription);
		}

		protected static class Trivial extends Default {
			protected Trivial(TypeDescription type, MethodDescription method, List<? extends TypeDescription> latentTypes, boolean expandFrames) {
				super(type, method, Collections.emptyList(), latentTypes, Collections.emptyList(), Collections.emptyList(), expandFrames, 0);
			}

			@Override
			public void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
				methodVisitor.visitFrame(type, localVariableLength, localVariable, stackSize, stack);
			}

			@Override
			public void injectReturnFrame(MethodVisitor methodVisitor) {
				throw new IllegalStateException("Did not expect return frame for " + method);
			}

			@Override
			public void injectExceptionFrame(MethodVisitor methodVisitor) {
				throw new IllegalStateException("Did not expect exception frame for " + method);
			}

			@Override
			public void injectCompletionFrame(MethodVisitor methodVisitor) {
				throw new IllegalStateException("Did not expect completion frame for " + method);
			}

			@Override
			public ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
				throw new IllegalStateException("Did not expect exit advice " + adviceMethod + " for " + method);
			}

			@Override
			public void injectInitializationFrame(MethodVisitor methodVisitor) {
				// ignored
			}

			@Override
			public void injectStartFrame(MethodVisitor methodVisitor) {
				// ignored
			}

			@Override
			public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
				throw new IllegalStateException("Did not expect post completion frame for " + method);
			}
		}

		protected abstract static class WithPreservedArguments extends Default {
			protected boolean allowCompactCompletionFrame;

			public WithPreservedArguments(TypeDescription type, MethodDescription method, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> latentTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes, boolean expandFrames, boolean allowCompactCompletionFrame) {
				super(type, method, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames, 0);
				this.allowCompactCompletionFrame = allowCompactCompletionFrame;
			}

			@Override
			protected void translateFrame(MethodVisitor methodVisitor, TranslationMode translationMode, MethodDescription methodDescription, List<? extends TypeDescription> additionalTypes, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
				if (type == Opcodes.NULL && localVariableLength > 0 && localVariable[0] != Opcodes.UNINITIALIZED_THIS) {
					allowCompactCompletionFrame = true;
				}
				super.translateFrame(methodVisitor, translationMode, methodDescription, additionalTypes, type, localVariableLength, localVariable, stackSize, stack);
			}

			@Override
			public StackMapFrameHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
				return new ForAdvice(adviceMethod,
						CompoundList.of(initialTypes, preMethodTypes, postMethodTypes),
						Collections.emptyList(),
						Collections.emptyList(),
						TranslationMode.EXIT,
						Initialization.INITIALIZED);
			}

			@Override
			public void injectReturnFrame(MethodVisitor methodVisitor) {
				if (!expandFrames && currentFrameDivergence == 0) {
					if (method.getReturnType().represents(void.class)) {
						methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
					}
					else {
						methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Initialization.INITIALIZED.toFrame(method.getReturnType().asErasure())});
					}
				}
				else {
					injectFullFrame(methodVisitor, Initialization.INITIALIZED, CompoundList.of(initialTypes, preMethodTypes),
							method.getReturnType().represents(void.class)
									? Collections.emptyList()
									: Collections.singletonList(method.getReturnType().asErasure()));
				}
			}

			@Override
			public void injectExceptionFrame(MethodVisitor methodVisitor) {
				if (!expandFrames && currentFrameDivergence == 0) {
					methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Type.getInternalName(Throwable.class)});
				}
				else {
					injectFullFrame(methodVisitor, Initialization.INITIALIZED, CompoundList.of(initialTypes, preMethodTypes), List.of(TypeDescription.THROWABLE));
				}
			}

			@Override
			public void injectCompletionFrame(MethodVisitor methodVisitor) {
				if (allowCompactCompletionFrame && !expandFrames && currentFrameDivergence == 0 && postMethodTypes.size() < 4) {
					if (postMethodTypes.isEmpty()) {
						methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
					}
					else {
						Object[] local = postMethodTypes.stream().map(Initialization.INITIALIZED::toFrame).toArray();
						methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
					}
				}
				else {
					injectFullFrame(methodVisitor, Initialization.INITIALIZED, CompoundList.of(initialTypes, preMethodTypes, postMethodTypes), Collections.emptyList());
				}
			}

			@Override
			public void injectPostCompletionFrame(MethodVisitor methodVisitor) {
				if (!expandFrames && currentFrameDivergence == 0) {
					methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
				}
				else {
					injectFullFrame(methodVisitor, Initialization.INITIALIZED,
							CompoundList.of(initialTypes, preMethodTypes, postMethodTypes), List.of());
				}
			}

			@Override
			public void injectInitializationFrame(MethodVisitor methodVisitor) {
				if (!initialTypes.isEmpty()) {
					if (!expandFrames && initialTypes.size() < 4) {
						Object[] local = initialTypes.stream().map(Initialization.INITIALIZED::toFrame).toArray();
						methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
					}
					else {
						Object[] local = new Object[method.isStatic() ? 0 : 1 + method.getParameters().size() + initialTypes.size()];
						int index = 0;
						if (method.isConstructor()) {
							local[index++] = Opcodes.UNINITIALIZED_THIS;
						}
						else if (!method.isStatic()) {
							local[index++] = Initialization.INITIALIZED.toFrame(type);
						}
						for (TypeDescription typeDescription : method.getParameters().asTypeList().asErasures()) {
							local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
						}
						for (TypeDescription typeDescription : initialTypes) {
							local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
						}
						methodVisitor.visitFrame(expandFrames ? Opcodes.F_NEW : Opcodes.F_FULL, local.length, local, EMPTY.length, EMPTY);
					}
				}
			}

			protected static class WithoutArgumentCopy extends WithPreservedArguments {

				protected WithoutArgumentCopy(TypeDescription type, MethodDescription method, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> latentTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes, boolean expandFrames, boolean allowCompactCompletionFrame) {
					super(type, method, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames, allowCompactCompletionFrame);
				}

				@Override
				public void injectStartFrame(MethodVisitor methodVisitor) {
					// ignored
				}

				@Override
				public void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
					translateFrame(methodVisitor, TranslationMode.COPY, method,
							CompoundList.of(initialTypes, preMethodTypes), type, localVariableLength, localVariable, stackSize, stack);
				}
			}

			protected static class WithArgumentCopy extends WithoutArgumentCopy {

				protected WithArgumentCopy(TypeDescription type, MethodDescription method, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> latentTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes, boolean expandFrames) {
					super(type, method, initialTypes, latentTypes, preMethodTypes, postMethodTypes, expandFrames, true);
				}

				@Override
				public void injectStartFrame(MethodVisitor methodVisitor) {
					if (!method.isStatic() || !method.getParameters().isEmpty()) {
						if (!expandFrames && (method.isStatic() ? 0 : 1) + method.getParameters().size() < 4) {
							Object[] local = new Object[(method.isStatic() ? 0 : 1) + method.getParameters().size()];
							int index = 0;
							if (method.isConstructor()) {
								local[index++] = Opcodes.UNINITIALIZED_THIS;
							}
							else if (!method.isStatic()) {
								local[index++] = Initialization.INITIALIZED.toFrame(type);
							}
							for (TypeDescription typeDescription : method.getParameters().asTypeList().asErasures()) {
								local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
						}
						else {
							Object[] local = new Object[(method.isStatic() ? 0 : 2) + method.getParameters().size() * 2 + initialTypes.size() + preMethodTypes.size()];
							int index = 0;
							if (method.isConstructor()) {
								local[index++] = Opcodes.UNINITIALIZED_THIS;
							}
							else if (!method.isStatic()) {
								local[index++] = Initialization.INITIALIZED.toFrame(type);
							}
							for (TypeDescription typeDescription : method.getParameters().asTypeList().asErasures()) {
								local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							for (TypeDescription typeDescription : initialTypes) {
								local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							for (TypeDescription typeDescription : preMethodTypes) {
								local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							if (method.isConstructor()) {
								local[index++] = Opcodes.UNINITIALIZED_THIS;
							}
							else if (!method.isStatic()) {
								local[index++] = Initialization.INITIALIZED.toFrame(type);
							}
							for (TypeDescription typeDescription : method.getParameters().asTypeList().asErasures()) {
								local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							methodVisitor.visitFrame(expandFrames ? Opcodes.F_NEW : Opcodes.F_FULL, local.length, local, EMPTY.length, EMPTY);
						}
					}
					currentFrameDivergence = (method.isStatic() ? 0 : 1) + method.getParameters().size();
				}

				@Override
				public void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
					switch (type) {
						case Opcodes.F_SAME, Opcodes.F_SAME1 -> {}
						case Opcodes.F_APPEND -> currentFrameDivergence += localVariableLength;
						case Opcodes.F_CHOP -> currentFrameDivergence -= localVariableLength;
						case Opcodes.F_FULL, Opcodes.F_NEW -> {
							Object[] translated = new Object[localVariableLength
									+ (method.isStatic() ? 0 : 1)
									+ method.getParameters().size()
									+ initialTypes.size()
									+ preMethodTypes.size()];
							int index = 0;
							if (method.isConstructor()) {
								Initialization initialization = Initialization.INITIALIZED;
								for (int i = 0; i < localVariableLength; i++) {
									if (localVariable[i] == Opcodes.UNINITIALIZED_THIS) {
										initialization = Initialization.UNITIALIZED;
										break;
									}
								}
								translated[index++] = initialization.toFrame(this.type);
							}
							else if (!method.isStatic()) {
								translated[index++] = Initialization.INITIALIZED.toFrame(this.type);
							}
							for (TypeDescription typeDescription : method.getParameters().asTypeList().asErasures()) {
								translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							for (TypeDescription typeDescription : initialTypes) {
								translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							for (TypeDescription typeDescription : preMethodTypes) {
								translated[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
							}
							if (localVariableLength > 0) {
								System.arraycopy(localVariable, 0, translated, index, localVariableLength);
							}
							localVariableLength = translated.length;
							localVariable = translated;
							currentFrameDivergence = localVariableLength;
						}
						default -> throw new IllegalArgumentException("Unexpected frame type: " + type);
					}
					methodVisitor.visitFrame(type, localVariableLength, localVariable, stackSize, stack);
				}
			}
		}

		protected class ForAdvice implements StackMapFrameHandler.ForAdvice {
			protected final MethodDescription.InDefinedShape adviceMethod;
			protected final List<? extends TypeDescription> startTypes;
			protected final List<? extends TypeDescription> intermediateTypes;
			protected final List<? extends TypeDescription> endTypes;
			protected final TranslationMode translationMode;
			protected final Initialization initialization;
			protected boolean intermedate = false;

			public ForAdvice(MethodDescription.InDefinedShape adviceMethod, List<? extends TypeDescription> startTypes, List<? extends TypeDescription> intermediateTypes, List<? extends TypeDescription> endTypes, TranslationMode translationMode, Initialization initialization) {
				this.adviceMethod = adviceMethod;
				this.startTypes = startTypes;
				this.intermediateTypes = intermediateTypes;
				this.endTypes = endTypes;
				this.translationMode = translationMode;
				this.initialization = initialization;
			}

			@Override
			public void translateFrame(MethodVisitor methodVisitor, int type, int localVariableLength, Object[] localVariable, int stackSize, Object[] stack) {
				StackMapFrameHandler.Default.this.translateFrame(
						methodVisitor,
						translationMode,
						adviceMethod,
						startTypes,
						type,
						localVariableLength,
						localVariable,
						stackSize,
						stack
				);
			}

			@Override
			public void injectReturnFrame(MethodVisitor methodVisitor) {
				if (!expandFrames && currentFrameDivergence == 0) {
					if (adviceMethod.getReturnType().represents(void.class)) {
						methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
					}
					else {
						methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Initialization.INITIALIZED.toFrame(adviceMethod.getReturnType().asErasure())});
					}
				}
				else {
					injectFullFrame(methodVisitor, initialization, startTypes,
							adviceMethod.getReturnType().represents(void.class)
									? Collections.emptyList()
									: Collections.singletonList(adviceMethod.getReturnType().asErasure()));
				}
			}

			@Override
			public void injectExceptionFrame(MethodVisitor methodVisitor) {
				if (!expandFrames && currentFrameDivergence == 0) {
					methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Type.getInternalName(Throwable.class)});
				}
				else {
					injectFullFrame(methodVisitor, initialization, startTypes, Collections.singletonList(TypeDescription.THROWABLE));
				}
			}

			@Override
			public void injectCompletionFrame(MethodVisitor methodVisitor) {
				if (expandFrames) {
					injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, endTypes), Collections.emptyList());
				}
				else if (currentFrameDivergence == 0 && (intermedate || endTypes.size() < 4)) {
					if (intermedate || endTypes.isEmpty()) {
						methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
					}
					else {
						var local = new Object[endTypes.size()];
						var index = 0;
						for (var typeDescription : endTypes) {
							local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
						}
						methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
					}
				}
				else if (currentFrameDivergence < 3 && endTypes.isEmpty()) {
					methodVisitor.visitFrame(Opcodes.F_CHOP, currentFrameDivergence, EMPTY, EMPTY.length, EMPTY);
					currentFrameDivergence = 0;
				}
				else {
					injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, endTypes), Collections.emptyList());
				}
			}

			@Override
			public void injectIntermediateFrame(MethodVisitor methodVisitor, List<? extends TypeDescription> stack) {
				if (expandFrames) {
					injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, intermediateTypes), stack);
				}
				else if (intermedate && stack.size() < 2) {
					if (stack.isEmpty()) {
						methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
					}
					else {
						methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Initialization.INITIALIZED.toFrame(stack.get(0))});
					}
				}
				else if (currentFrameDivergence == 0 && intermediateTypes.size() < 4
						&& (stack.isEmpty() || stack.size() < 2 && intermediateTypes.isEmpty())) {
					if (intermediateTypes.isEmpty()) {
						if (stack.isEmpty()) {
							methodVisitor.visitFrame(Opcodes.F_SAME, EMPTY.length, EMPTY, EMPTY.length, EMPTY);
						}
						else {
							methodVisitor.visitFrame(Opcodes.F_SAME1, EMPTY.length, EMPTY, 1, new Object[] {Initialization.INITIALIZED.toFrame(stack.get(0))});
						}
					}
					else {
						var local = new Object[intermediateTypes.size()];
						var index = 0;
						for (var typeDescription : intermediateTypes) {
							local[index++] = Initialization.INITIALIZED.toFrame(typeDescription);
						}
						methodVisitor.visitFrame(Opcodes.F_APPEND, local.length, local, EMPTY.length, EMPTY);
					}
				}
				else if (currentFrameDivergence < 3 && intermediateTypes.isEmpty() && stack.isEmpty()) {
					methodVisitor.visitFrame(Opcodes.F_CHOP, currentFrameDivergence, EMPTY, EMPTY.length, EMPTY);
				}
				else {
					injectFullFrame(methodVisitor, initialization, CompoundList.of(startTypes, intermediateTypes), stack);
				}
				currentFrameDivergence = intermediateTypes.size() - endTypes.size();
				intermedate = true;
			}
		}
	}
}
