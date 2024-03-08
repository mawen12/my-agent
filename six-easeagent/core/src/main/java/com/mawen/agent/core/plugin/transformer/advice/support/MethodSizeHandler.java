package com.mawen.agent.core.plugin.transformer.advice.support;

import java.util.List;

import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackSize;
import net.bytebuddy.jar.asm.ClassWriter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
public interface MethodSizeHandler {
	int UNDEFINED_SIZE = Short.MAX_VALUE;

	void requireStackSize(int stackSize);

	void requireLocalVariableLength(int localVariableLength);

	interface ForInstrumentedMethod extends MethodSizeHandler {
		ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod);

		ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod);

		int compoundStackSize(int stackSize);

		int compoundLocalVariableLength(int localVariableLength);
	}

	interface ForAdvice extends MethodSizeHandler {

		void requireStackSizePadding(int stackSizePadding);

		void requireLocalVariableLengthPadding(int localVariableLengthPadding);

		void recordMaxima(int stackSize, int localVariableLength);
	}

	enum NoOp implements ForInstrumentedMethod, ForAdvice {
		INSTANCE;


		@Override
		public ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
			return this;
		}

		@Override
		public ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
			return this;
		}

		@Override
		public int compoundStackSize(int stackSize) {
			return UNDEFINED_SIZE;
		}

		@Override
		public int compoundLocalVariableLength(int localVariableLength) {
			return UNDEFINED_SIZE;
		}

		@Override
		public void requireStackSize(int stackSize) {
			// ignored
		}

		@Override
		public void requireLocalVariableLength(int localVariableLength) {
			// ignored
		}

		@Override
		public void requireStackSizePadding(int stackSizePadding) {
			// ignored
		}

		@Override
		public void requireLocalVariableLengthPadding(int localVariableLengthPadding) {
			// ignored
		}

		@Override
		public void recordMaxima(int stackSize, int localVariableLength) {
			// ignored
		}
	}

	abstract class Default implements ForInstrumentedMethod {

		protected final MethodDescription instrumentedMethod;
		protected final List<? extends TypeDescription> initialTypes;
		protected final List<? extends TypeDescription> preMethodTypes;
		protected final List<? extends TypeDescription> postMethodTypes;
		protected int stackSize;
		protected int localVariableLength;

		public Default(MethodDescription instrumentedMethod, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes) {
			this.instrumentedMethod = instrumentedMethod;
			this.initialTypes = initialTypes;
			this.preMethodTypes = preMethodTypes;
			this.postMethodTypes = postMethodTypes;
		}

		public static ForInstrumentedMethod of(MethodDescription instrumentedMethod,
				List<? extends TypeDescription> initialTypes,
				List<? extends TypeDescription> preMethodTypes,
				List<? extends TypeDescription> postMethodTypes,
				boolean copyArguments,
				int writerFlags) {
			if ((writerFlags & (ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)) != 0) {
				return NoOp.INSTANCE;
			}
			else if (copyArguments) {
				return new WithCopiedArguments(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
			}
			else {
				return new WithRetainedArguments(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
			}
		}

		@Override
		public MethodSizeHandler.ForAdvice bindEnter(MethodDescription.InDefinedShape adviceMethod) {
			return new ForAdvice(adviceMethod, instrumentedMethod.getStackSize() + StackSize.of(initialTypes));
		}

		@Override
		public void requireStackSize(int stackSize) {
			this.stackSize = Math.max(this.stackSize, stackSize);
		}

		@Override
		public void requireLocalVariableLength(int localVariableLength) {
			this.localVariableLength = Math.max(this.localVariableLength, localVariableLength);
		}

		@Override
		public int compoundStackSize(int stackSize) {
			return Math.max(this.stackSize, stackSize);
		}

		@Override
		public int compoundLocalVariableLength(int localVariableLength) {
			return Math.max(this.localVariableLength, localVariableLength
					+ StackSize.of(initialTypes)
					+ StackSize.of(preMethodTypes)
					+ StackSize.of(postMethodTypes)
			);
		}

		protected static class WithRetainedArguments extends Default {
			public WithRetainedArguments(MethodDescription instrumentedMethod, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes) {
				super(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
			}

			@Override
			public MethodSizeHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
				return new ForAdvice(adviceMethod, instrumentedMethod.getStackSize()
				+ StackSize.of(postMethodTypes)
				+ StackSize.of(initialTypes)
				+ StackSize.of(preMethodTypes));
			}

			@Override
			public int compoundLocalVariableLength(int localVariableLength) {
				return Math.max(this.localVariableLength, localVariableLength
						+ StackSize.of(preMethodTypes)
						+ StackSize.of(initialTypes)
						+ StackSize.of(postMethodTypes));
			}
		}

		protected static class WithCopiedArguments extends Default {
			public WithCopiedArguments(MethodDescription instrumentedMethod, List<? extends TypeDescription> initialTypes, List<? extends TypeDescription> preMethodTypes, List<? extends TypeDescription> postMethodTypes) {
				super(instrumentedMethod, initialTypes, preMethodTypes, postMethodTypes);
			}

			@Override
			public MethodSizeHandler.ForAdvice bindExit(MethodDescription.InDefinedShape adviceMethod) {
				return new ForAdvice(adviceMethod, 2 * instrumentedMethod.getStackSize()
						+ StackSize.of(initialTypes)
						+ StackSize.of(preMethodTypes)
						+ StackSize.of(postMethodTypes));
			}

			@Override
			public int compoundLocalVariableLength(int localVariableLength) {
				return Math.max(this.localVariableLength, localVariableLength
						+ instrumentedMethod.getStackSize()
						+ StackSize.of(preMethodTypes)
						+ StackSize.of(initialTypes)
						+ StackSize.of(postMethodTypes));
			}
		}

		protected class ForAdvice implements MethodSizeHandler.ForAdvice {
			private final MethodDescription.InDefinedShape adviceMethod;
			private final int baseLocalVariableLength;
			private int stackSizePadding;
			private int localVariableLengthPadding;

			public ForAdvice(MethodDescription.InDefinedShape adviceMethod, int baseLocalVariableLength) {
				this.adviceMethod = adviceMethod;
				this.baseLocalVariableLength = baseLocalVariableLength;
			}

			@Override
			public void requireStackSize(int stackSize) {
				Default.this.requireStackSize(stackSize);
			}

			@Override
			public void requireLocalVariableLength(int localVariableLength) {
				Default.this.requireLocalVariableLength(localVariableLength);
			}

			@Override
			public void requireStackSizePadding(int stackSizePadding) {
				this.stackSizePadding = Math.max(this.stackSizePadding, stackSizePadding);
			}

			@Override
			public void requireLocalVariableLengthPadding(int localVariableLengthPadding) {
				this.localVariableLengthPadding = Math.max(this.localVariableLengthPadding, localVariableLengthPadding);
			}

			@Override
			public void recordMaxima(int stackSize, int localVariableLength) {
				Default.this.requireStackSize(stackSize + stackSizePadding);
				Default.this.requireLocalVariableLength(localVariableLength
						- adviceMethod.getStackSize()
						+ baseLocalVariableLength
						+ localVariableLengthPadding);
			}
		}
	}
}
