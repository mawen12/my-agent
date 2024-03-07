package com.mawen.agent.core.plugin.transformer.advice;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntSupplier;
import java.util.stream.IntStream;

import com.mawen.agent.core.plugin.registry.AdviceRegistry;
import com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory.Illegal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.ClassVisitor;
import net.bytebuddy.jar.asm.Label;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.jar.asm.TypePath;
import net.bytebuddy.utility.CompoundList;
import net.bytebuddy.utility.OpenedClassReader;
import net.bytebuddy.utility.visitor.ExceptionTableSensitiveMethodVisitor;
import net.bytebuddy.utility.visitor.FramePaddingMethodVisitor;
import net.bytebuddy.utility.visitor.LineNumberPrependingMethodVisitor;
import net.bytebuddy.utility.visitor.StackAwareMethodVisitor;

import static com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.*;
import static net.bytebuddy.description.method.MethodDescription.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class AgentAdvice extends Advice {

	private static final ClassReader UNDEFINED = null;

	private static final InDefinedShape SKIP_ON;
	private static final InDefinedShape PREPEND_LINE_NUMBER;
	private static final InDefinedShape INLINE_ENTER;
	private static final InDefinedShape SUPPRESS_ENTER;
	private static final InDefinedShape REPEAT_ON;
	public static final InDefinedShape ON_THROWABLE;
	private static final InDefinedShape BACKUP_ARGUMENTS;
	private static final InDefinedShape INLINE_EXIT;
	private static final InDefinedShape SUPPRESS_EXIT;

	static {
		MethodList<InDefinedShape> enter = TypeDescription.ForLoadedType.of(OnMethodEnter.class).getDeclaredMethods();
		SKIP_ON = enter.filter(named("skipOn")).getOnly();
		PREPEND_LINE_NUMBER = enter.filter(named("prependLineNumber")).getOnly();
		INLINE_ENTER = enter.filter(named("inline")).getOnly();
		SUPPRESS_ENTER = enter.filter(named("suppress")).getOnly();

		MethodList<InDefinedShape> exit = TypeDescription.ForLoadedType.of(OnMethodExit.class).getDeclaredMethods();
		REPEAT_ON = exit.filter(named("repeatOn")).getOnly();
		ON_THROWABLE = exit.filter(named("onThrowable")).getOnly();
		BACKUP_ARGUMENTS = exit.filter(named("backupArguments")).getOnly();
		INLINE_EXIT = exit.filter(named("inline")).getOnly();
		SUPPRESS_EXIT = exit.filter(named("suppress")).getOnly();
	}

	private final Dispatcher.Resolved.ForMethodEnter methodEnter;
	private final Dispatcher.Resolved.ForMethodExit methodExit;
	private final Dispatcher.Resolved.ForMethodExit methodExitNonThrowable;
	private final Assigner assigner;
	private final ExceptionHandler exceptionHandler;
	private final Implementation delegate;

	protected AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter, Dispatcher.Resolved.ForMethodExit methodExit) {
		this(methodEnter, methodExit, null, Assigner.DEFAULT,
				ExceptionHandler.Default.SUPPRESSING, SuperMethodCall.INSTANCE);
	}

	protected AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter, Dispatcher.Resolved.ForMethodExit methodExitNonThrowable,
			Dispatcher.Resolved.ForMethodExit methodExit) {
		this(methodEnter, methodExit, methodExitNonThrowable, Assigner.DEFAULT,
				ExceptionHandler.Default.SUPPRESSING, SuperMethodCall.INSTANCE);
	}

	private AgentAdvice(Dispatcher.Resolved.ForMethodEnter methodEnter, Dispatcher.Resolved.ForMethodExit methodExit,
			Dispatcher.Resolved.ForMethodExit methodExitNonThrowable, Assigner assigner,
			ExceptionHandler exceptionHandler, Implementation delegate) {
		super(null, null);
		this.methodEnter = methodEnter;
		this.methodExit = methodExit;
		this.methodExitNonThrowable = methodExitNonThrowable;
		this.assigner = assigner;
		this.exceptionHandler = exceptionHandler;
		this.delegate = delegate;
	}

	public static boolean isNoExceptionHandler(TypeDescription t) {
		return t.getName().endsWith("NoExceptionHandler");
	}

	protected static AgentAdvice tto(TypeDescription advice, PostProcessor.Factory postProcessorFactory,
			ClassFileLocator classFileLocator,
			List<? extends OffsetMapping.Factory<?>> userFactories,
			Delegator delegator) {
		Dispatcher.Unresolved methodEnter = Dispatcher.Inactive.INSTANCE;
		Dispatcher.Unresolved methodExit = Dispatcher.Inactive.INSTANCE;
		Dispatcher.Unresolved methodExitNoException = Dispatcher.Inactive.INSTANCE;

		for (InDefinedShape methodDescription : advice.getDeclaredMethods()) {
			methodEnter = locate(OnMethodEnter.class, INLINE_ENTER, methodEnter, methodDescription, delegator);
			AnnotationDescription.Loadable<OnMethodExit> al = methodDescription.getDeclaredAnnotations().ofType(OnMethodExit.class);
			if (al != null) {
				TypeDescription throwable = al.getValue(ON_THROWABLE).resolve(TypeDescription.class);
				if (isNoExceptionHandler(throwable)) {
					methodExitNoException = locate(OnMethodExit.class, INLINE_EXIT, methodExitNoException, methodDescription, delegator);
				}
				else {
					methodExit = locate(OnMethodExit.class, INLINE_EXIT, methodExit, methodDescription, delegator);
				}
			}
		}

		if (methodExit == Dispatcher.Inactive.INSTANCE) {
			methodEnter = methodExitNoException;
		}
		if (!methodEnter.isAlive() && !methodExit.isAlive() && !methodExitNoException.isAlive()) {
			throw new IllegalArgumentException("No advice defined by " + advice);
		}
		try {
			ClassReader classReader = methodEnter.isBinary() || methodEnter.isBinary()
					? OpenedClassReader.of(classFileLocator.locate(advice.getName()).resolve())
					: UNDEFINED;
			return new AgentAdvice(methodEnter.asMethodEnter(userFactories, classReader, methodExit, postProcessorFactory),
					methodExitNoException.asMethodExit(userFactories, classReader, methodEnter, postProcessorFactory),
					methodExit.asMethodExit(userFactories, classReader, methodEnter, postProcessorFactory));
		}
		catch (IOException e) {
			throw new IllegalStateException("Error reading class file of " + advice, e);
		}
	}

	public static AgentAdvice.WithCustomMapping withCustomMapping() {
		return new WithCustomMapping();
	}

	private static Dispatcher.Unresolved locate(Class<? extends Annotation> type,
			MethodDescription.InDefinedShape property,
			Dispatcher.Unresolved dispatcher,
			MethodDescription.InDefinedShape methodDescription,
			Delegator delegator) {
		AnnotationDescription.Loadable<? extends Annotation> annotation = methodDescription.getDeclaredAnnotations().ofType(type);
		if (annotation == null) {
			return dispatcher;
		}
		else if (dispatcher.isAlive()) {
			throw new IllegalStateException("Duplicate advice for " + dispatcher + " and " + methodDescription);
		}
		else if (!methodDescription.isStatic()) {
			throw new IllegalStateException("Advice for " + methodDescription + " is not static");
		}
		else {
			return new Dispatcher.Inlining(methodDescription);
		}
	}

	@Override
	public Advice withAssigner(Assigner assigner) {
		return new AgentAdvice(methodEnter, methodExitNonThrowable,
				methodExit, assigner, exceptionHandler, delegate);
	}

	@Override
	public Advice withExceptionHandler(ExceptionHandler exceptionHandler) {
		return new AgentAdvice(methodEnter, methodExitNonThrowable,
				methodExit, assigner, exceptionHandler, delegate);
	}

	@Override
	protected MethodVisitor doWrap(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Context implementationContext, int writerFlags, int readerFlags) {
		Dispatcher.Resolved.ForMethodExit exit;
		if (instrumentedMethod.isConstructor()) {
			exit = methodExitNonThrowable;
		}
		else {
			exit = methodExit;
		}

		if (AdviceRegistry.check(instrumentedType, instrumentedMethod, methodEnter, exit) == 0) {
			return methodVisitor;
		}

		methodVisitor = new FramePaddingMethodVisitor(methodEnter.isPrependLineNumber()
				? new LineNumberPrependingMethodVisitor(methodVisitor)
				: methodVisitor);

		if (instrumentedMethod.isConstructor()) {
			return new WithExitAdvice.WithoutExceptionHandling();
		}
		else {

		}
	}

	// =========================================

	@AllArgsConstructor(access = AccessLevel.PROTECTED)
	@HashCodeAndEqualsPlugin.Enhance
	public static class WithCustomMapping extends Advice.WithCustomMapping {
		private final PostProcessor.Factory postProcessorFactory;
		private final Delegator delegator;
		private final Map<Class<? extends Annotation>, OffsetMapping.Factory<?>> offsetMappings;

		public WithCustomMapping() {
			this(PostProcessor.NoOp.INSTANCE, Delegator.ForStaticInvocation.INSTANCE, Collections.emptyMap());
		}

		@Override
		public <T extends Annotation> WithCustomMapping bind(Class<T> type, Object value) {
			return bind(OffsetMapping.ForStackManipulation.Factory.of(type, value));
		}

		public WithCustomMapping bind(OffsetMapping.Factory<?> offsetMapping) {
			Map<Class<? extends Annotation>, OffsetMapping.Factory<?>> offsetMappings = new HashMap<>(this.offsetMappings);
			if (!offsetMapping.getAnnotationType().isAnnotation()) {
				throw new IllegalArgumentException("Not an annotation type: " + offsetMapping.getAnnotationType());
			}
			else if (offsetMappings.put(offsetMapping.getAnnotationType(), offsetMapping) != null) {
				throw new IllegalArgumentException("Annotation type already mapped: " + offsetMapping.getAnnotationType());
			}
			return new WithCustomMapping(postProcessorFactory, delegator, offsetMappings);
		}

		@Override
		public Advice to(TypeDescription advice, ClassFileLocator classFileLocator) {
			return AgentAdvice.tto(advice, postProcessorFactory, classFileLocator, new ArrayList<>(offsetMappings.values()), delegator);
		}
	}

	protected abstract static class WithExitAdvice extends AdviceVisitor {
		protected final Label returnHandler;

		protected WithExitAdvice(MethodVisitor methodVisitor,
				Implementation.Context implementationContext,
				Assigner assigner,
				StackManipulation exceptionHandler,
				TypeDescription instrumentedType,
				MethodDescription instrumentedMethod,
				Dispatcher.Resolved.ForMethodEnter methodEnter,
				Dispatcher.Resolved.ForMethodExit methodExit,
				List<? extends TypeDescription> postMethodTypes,
				int writerFlags,
				int readerFlags) {
			super(new StackAwareMethodVisitor(methodVisitor, instrumentedMethod),
					implementationContext, assigner, exceptionHandler, instrumentedType, instrumentedMethod,
					methodEnter, methodExit, postMethodTypes, writerFlags, readerFlags);
		}

		@Override
		protected void onUserPrepare() {

		}

		@Override
		protected void onUserStart() {

		}

		@Override
		protected void onUserEnd() {

		}

		@Override
		public void apply(MethodVisitor methodVisitor) {

		}
	}

	protected abstract static class AdviceVisitor
			extends ExceptionTableSensitiveMethodVisitor implements Dispatcher.RelocationHandler.Relocation {

	}

	public interface Dispatcher {
		MethodVisitor IGNORE_METHOD = null;
		AnnotationVisitor IGNORE_ANNOTATION = null;

		boolean isAlive();

		TypeDefinition getAdviceType();

		interface Unresolved extends Dispatcher {
			boolean isBinary();

			Map<String, TypeDefinition> getNamedTypes();

			Resolved.ForMethodEnter asMethodEnter(List<? extends Advice.OffsetMapping.Factory<?>> userFactories,
					ClassReader classReader,
					Unresolved methodExit,
					Advice.PostProcessor.Factory postProcessorFactory);

			Resolved.ForMethodExit asMethodExit(List<? extends Advice.OffsetMapping.Factory<?>> userFactories,
					ClassReader classReader,
					Unresolved methodEnter,
					Advice.PostProcessor.Factory postProcessorFactory);
		}

		interface SuppressionHandler {

			Bound bind(StackManipulation exceptionHandler);

			interface Bound {
				void onPrepare(MethodVisitor methodVisitor);

				void onStart(MethodVisitor methodVisitor);

				void onEnd(MethodVisitor methodVisitor,
						Implementation.Context implementationContext,
						com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler,
						TypeDefinition returnType);

				void onEndWithSkip(MethodVisitor methodVisitor,
						Implementation.Context implementationContext,
						com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler,
						TypeDefinition returnType);
			}

			enum NoOp implements SuppressionHandler, Bound {
				INSTANCE;

				@Override
				public Bound bind(StackManipulation exceptionHandler) {
					return this;
				}

				@Override
				public void onPrepare(MethodVisitor methodVisitor) {
					// ignored
				}

				@Override
				public void onStart(MethodVisitor methodVisitor) {
					// ignored
				}

				@Override
				public void onEnd(MethodVisitor methodVisitor, Implementation.Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, TypeDefinition returnType) {
					// ignored
				}

				@Override
				public void onEndWithSkip(MethodVisitor methodVisitor, Implementation.Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, TypeDefinition returnType) {
					// ignored
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			class Suppressing implements SuppressionHandler {
				private final TypeDescription suppressedType;

				protected static SuppressionHandler of(TypeDescription suppressedType) {
					return isNoExceptionHandler(suppressedType)
							? NoOp.INSTANCE
							: new Suppressing(suppressedType);
				}

				@Override
				public Bound bind(StackManipulation exceptionHandler) {
					return new Bound(suppressedType, exceptionHandler);
				}

				@AllArgsConstructor(access = AccessLevel.PROTECTED)
				protected static class Bound implements SuppressionHandler.Bound {
					private final TypeDescription suppressedType;
					private final StackManipulation exceptionHandler;
					private final Label startOfMethod = new Label();
					private final Label endOfMethod = new Label();

					@Override
					public void onPrepare(MethodVisitor methodVisitor) {
						methodVisitor.visitTryCatchBlock(startOfMethod, endOfMethod, endOfMethod, suppressedType.getInternalName());
					}

					@Override
					public void onStart(MethodVisitor methodVisitor) {
						methodVisitor.visitLabel(startOfMethod);
					}

					@Override
					public void onEnd(MethodVisitor methodVisitor, Implementation.Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, TypeDefinition returnType) {
						methodVisitor.visitLabel(endOfMethod);
						stackMapFrameHandler.injectExceptionFrame(methodVisitor);
						methodSizeHandler.requireStackSize(1 + exceptionHandler.apply(methodVisitor, implementationContext).getMaximalSize());
						if (returnType.represents(boolean.class)
								|| returnType.represents(byte.class)
								|| returnType.represents(short.class)
								|| returnType.represents(char.class)
								|| returnType.represents(int.class)) {
							methodVisitor.visitInsn(Opcodes.ICONST_0);
						}
						else if (returnType.represents(long.class)) {
							methodVisitor.visitInsn(Opcodes.LCONST_0);
						}
						else if (returnType.represents(float.class)) {
							methodVisitor.visitInsn(Opcodes.FCONST_0);
						}
						else if (returnType.represents(double.class)) {
							methodVisitor.visitInsn(Opcodes.DCONST_0);
						}
						else if (returnType.represents(void.class)) {
							methodVisitor.visitInsn(Opcodes.ACONST_NULL);
						}
					}

					@Override
					public void onEndWithSkip(MethodVisitor methodVisitor, Implementation.Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, TypeDefinition returnType) {
						Label skipExceptionHandler = new Label();
						methodVisitor.visitJumpInsn(Opcodes.GOTO, skipExceptionHandler);
						onEnd(methodVisitor, implementationContext, methodSizeHandler, stackMapFrameHandler, returnType);
						methodVisitor.visitLabel(skipExceptionHandler);
						stackMapFrameHandler.injectReturnFrame(methodVisitor);
					}
				}
			}
		}

		interface RelocationHandler {

			Bound bind(MethodDescription instrumentedMethod, Relocation relocation);

			interface Relocation {

				void apply(MethodVisitor methodVisitor);

				@AllArgsConstructor
				@HashCodeAndEqualsPlugin.Enhance
				class ForLabel implements Relocation {
					private final Label label;

					@Override
					public void apply(MethodVisitor methodVisitor) {
						methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
					}
				}
			}

			interface Bound {
				int NO_REQUIRED_SIZE = 0;

				int apply(MethodVisitor methodVisitor, int offset);
			}

			enum Disabled implements RelocationHandler, Bound {
				INSTANCE;

				public Bound bind(MethodDescription instrumentMethod, Relocation relocation) {
					return this;
				}

				@Override
				public int apply(MethodVisitor methodVisitor, int offset) {
					return NO_REQUIRED_SIZE;
				}
			}

			@AllArgsConstructor
			enum ForValue implements RelocationHandler {
				INTEGER(Opcodes.ILOAD, Opcodes.IFNE, Opcodes.IFEQ, 0) {
					@Override
					protected void convertValue(MethodVisitor methodVisitor) {
						// ignored
					}
				},

				LONG(Opcodes.LLOAD, Opcodes.IFNE, Opcodes.IFEQ, 0) {
					@Override
					protected void convertValue(MethodVisitor methodVisitor) {
						methodVisitor.visitInsn(Opcodes.L2I);
					}
				},

				FLOAT(Opcodes.FLOAD, Opcodes.IFNE, Opcodes.IFEQ, 2) {
					@Override
					protected void convertValue(MethodVisitor methodVisitor) {
						methodVisitor.visitInsn(Opcodes.FCONST_0);
						methodVisitor.visitInsn(Opcodes.FCMPL);
					}
				},

				DOUBLE(Opcodes.DLOAD, Opcodes.IFNE, Opcodes.IFEQ, 4) {
					@Override
					protected void convertValue(MethodVisitor methodVisitor) {
						methodVisitor.visitInsn(Opcodes.DCONST_0);
						methodVisitor.visitInsn(Opcodes.DCMPL);
					}
				},

				REFERENCE(Opcodes.ALOAD, Opcodes.IFNONNULL, Opcodes.IFNULL, 0) {
					@Override
					protected void convertValue(MethodVisitor methodVisitor) {
						// ignored
					}
				};


				private final int load;
				private final int defaultJump;
				private final int nonDefaultJump;
				private final int requiredSize;

				protected static RelocationHandler of(TypeDefinition typeDefinition, boolean inverted) {
					ForValue skipDispatcher;
					if (typeDefinition.represents(long.class)) {
						skipDispatcher = LONG;
					}
					else if (typeDefinition.represents(float.class)) {
						skipDispatcher = FLOAT;
					}
					else if (typeDefinition.represents(double.class)) {
						skipDispatcher = DOUBLE;
					}
					else if (typeDefinition.represents(void.class)) {
						throw new IllegalStateException("Cannot skip on default value for void return type");
					}
					else if (typeDefinition.isPrimitive()) {
						skipDispatcher = INTEGER;
					}
					else {
						skipDispatcher = REFERENCE;
					}
					return inverted
							? skipDispatcher.new Inverted()
							: skipDispatcher;
				}

				protected abstract void convertValue(MethodVisitor methodVisitor);

				@Override
				public RelocationHandler.Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
					return new Bound(instrumentedMethod, relocation, false);
				}

				@HashCodeAndEqualsPlugin.Enhance(includeSyntheticFields = true)
				protected class Inverted implements RelocationHandler {
					@Override
					public Bound bind(MethodDescription instrumentedMethod, Relocation relocation) {
						return new ForValue.Bound(instrumentedMethod, relocation, true);
					}
				}

				@AllArgsConstructor(access = AccessLevel.PROTECTED)
				@HashCodeAndEqualsPlugin.Enhance(includeSyntheticFields = true)
				protected class Bound implements RelocationHandler.Bound {
					private final MethodDescription instrumentedMethod;
					private final Relocation relocation;
					private final boolean inverted;

					@Override
					public int apply(MethodVisitor methodVisitor, int offset) {
						if (instrumentedMethod.isConstructor()) {
							throw new IllegalStateException("Cannot skip code execution from constructor: " + instrumentedMethod);
						}
						methodVisitor.visitVarInsn(load, offset);
						convertValue(methodVisitor);
						Label noSkip = new Label();
						methodVisitor.visitJumpInsn(inverted
								? nonDefaultJump
								: defaultJump, noSkip);
						relocation.apply(methodVisitor);
						methodVisitor.visitLabel(noSkip);
						return requiredSize;
					}
				}
			}
		}

		interface Resolved extends Dispatcher {
			Map<String, TypeDefinition> getNamedTypes();

			Bound bind(TypeDescription instrumentedType,
					MethodDescription instrumentedMethod,
					MethodVisitor methodVisitor,
					Implementation.Context implementationContext,
					Assigner assigner,
					com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForInstrumentedMethod argumentHandler,
					com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
					com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
					StackManipulation exceptionHandler,
					Dispatcher.RelocationHandler.Relocation relocation);

			Map<Integer, com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping> getOffsetMapping();

			interface ForMethodEnter extends Resolved {
				boolean isPrependLineNumber();

				TypeDefinition getActualAdviceType();
			}

			interface ForMethodExit extends Resolved {
				TypeDescription getThrowable();

				Advice.ArgumentHandler.Factory getArgumentHandlerFactory();
			}

			@HashCodeAndEqualsPlugin.Enhance
			abstract class AbstractBase implements Resolved {
				protected final MethodDescription.InDefinedShape adviceMethod;
				protected final PostProcessor postProcessor;
				protected final Map<Integer, com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping> offsetMappings;
				protected final SuppressionHandler suppressionHandler;
				protected final RelocationHandler relocationHandler;

				protected AbstractBase(MethodDescription.InDefinedShape adviceMethod,
						Advice.PostProcessor postProcessor,
						List<? extends com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> factories,
						TypeDescription throwableType,
						TypeDescription relocatableType,
						Advice.OffsetMapping.Factory.AdviceType adviceType) {
					this.adviceMethod = adviceMethod;
					this.postProcessor = postProcessor;
					Map<TypeDescription, com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> offsetMappings = new HashMap<>();
					for (com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?> factory : factories) {
						offsetMappings.put(TypeDescription.ForLoadedType.of(factory.getAnnotationType()), factory);
					}
					this.offsetMappings = new LinkedHashMap<>();
					for (ParameterDescription.InDefinedShape parameter : adviceMethod.getParameters()) {
						Advice.OffsetMapping offsetMapping = null;
						for (AnnotationDescription annotationDescription : parameter.getDeclaredAnnotations()) {
							Advice.OffsetMapping.Factory<?> factory = offsetMappings.get(annotationDescription.getAnnotationType());
							if (factory != null) {
								Advice.OffsetMapping current = factory.make(parameter, (AnnotationDescription.Loadable) annotationDescription.prepare(factory.getAnnotationType()), adviceType);
								if (offsetMapping == null) {
									offsetMapping = current;
								}
								else {
									throw new IllegalStateException(parameter + " is bound to both " + current + " and " + offsetMapping);
								}
							}
						}
						this.offsetMappings.put(parameter.getOffset(), offsetMapping == null
								? new Advice.OffsetMapping.ForArgument.Unresolved(parameter)
								: offsetMapping);
					}
					suppressionHandler = SuppressionHandler.Suppressing.of(throwableType);
					relocationHandler = RelocationHandler.ForType.of(relocatableType, adviceMethod.getReturnType());
				}

				@Override
				public boolean isAlive() {
					return true;
				}

				@Override
				public Map<Integer, com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping> getOffsetMapping() {
					return this.offsetMappings;
				}
			}
		}

		interface Bound {
			void prepare();

			void initialize();

			void apply();
		}

		enum Inactive implements Dispatcher.Unresolved, Resolved.ForMethodEnter, Resolved.ForMethodExit, Bound {
			INSTANCE;


			@Override
			public boolean isAlive() {
				return false;
			}

			@Override
			public TypeDescription getAdviceType() {
				return TypeDescription.VOID;
			}

			@Override
			public boolean isBinary() {
				return false;
			}

			@Override
			public Map<String, TypeDefinition> getNamedTypes() {
				return Collections.emptyMap();
			}

			@Override
			public ForMethodEnter asMethodEnter(List<? extends Advice.OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodExit, Advice.PostProcessor.Factory postProcessorFactory) {
				return this;
			}

			@Override
			public ForMethodExit asMethodExit(List<? extends Advice.OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodEnter, Advice.PostProcessor.Factory postProcessorFactory) {
				return this;
			}

			@Override
			public Bound bind(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Implementation.Context implementationContext, Assigner assigner, Advice.ArgumentHandler.ForInstrumentedMethod argumentHandler, Advice.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler, Advice.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler, Advice.Dispatcher.RelocationHandler.Relocation relocation) {
				return this;
			}

			@Override
			public Map<Integer, com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping> getOffsetMapping() {
				return null;
			}

			@Override
			public boolean isPrependLineNumber() {
				return false;
			}

			@Override
			public TypeDescription getActualAdviceType() {
				return TypeDescription.VOID;
			}

			@Override
			public TypeDescription getThrowable() {
				return AgentAdvice.NoExceptionHandler.DESCRIPTION;
			}

			@Override
			public Advice.ArgumentHandler.Factory getArgumentHandlerFactory() {
				return Advice.ArgumentHandler.Factory.SIMPLE;
			}

			@Override
			public void prepare() {
				// ignored
			}

			@Override
			public void initialize() {
				// ignored
			}

			@Override
			public void apply() {
				// ignored
			}

		}

		@HashCodeAndEqualsPlugin.Enhance
		class Inlining implements Unresolved {
			protected final MethodDescription.InDefinedShape adviceMethod;
			private final Map<String, TypeDefinition> namedTypes;

			protected Inlining(MethodDescription.InDefinedShape adviceMethod) {
				this.adviceMethod = adviceMethod;
				this.namedTypes = new HashMap<>();
				for (ParameterDescription.InDefinedShape parameterDescription : adviceMethod.getParameters().filter(isAnnotatedWith(Advice.Local.class))) {
					parameterDescription.getDeclaredAnnotations()
							.ofType(Advice.Local.class).getValue(Advice.OffsetMapping.ForLocalValue.Factory.LOCAL)
				}
			}

			@Override
			public boolean isAlive() {
				return false;
			}

			@Override
			public TypeDefinition getAdviceType() {
				return null;
			}

			@Override
			public boolean isBinary() {
				return false;
			}

			@Override
			public Map<String, TypeDefinition> getNamedTypes() {
				return null;
			}

			@Override
			public Resolved.ForMethodEnter asMethodEnter(List<? extends Advice.OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodExit, Advice.PostProcessor.Factory postProcessorFactory) {
				return Resolved.ForMethodEnter.of(adviceMethod,
						postProcessorFactory.make(adviceMethod, false),
						namedTypes,
						userFactories,
						methodExit.getAdviceType(),
						classReader,
						methodExit.isAlive());
			}

			@Override
			public Resolved.ForMethodExit asMethodExit(List<? extends Advice.OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodEnter, Advice.PostProcessor.Factory postProcessorFactory) {
				return null;
			}

			protected abstract static class Resolved extends AgentAdvice.Dispatcher.Resolved.AbstractBase {
				protected final ClassReader classReader;

				protected Resolved(MethodDescription.InDefinedShape adviceMethod,
						Advice.PostProcessor postProcessor,
						List<? extends com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> factories,
						TypeDescription throwableType,
						TypeDescription relocatableType,
						ClassReader classReader) {
					super(adviceMethod, postProcessor, factories, throwableType, relocatableType, Advice.OffsetMapping.Factory.AdviceType.INLINING);
					this.classReader = classReader;
				}

				protected abstract Map<Integer, TypeDefinition> resolveInitializationTypes(Advice.ArgumentHandler argumentHandler);

				protected abstract MethodVisitor apply(MethodVisitor methodVisitor,
						Implementation.Context implementationContext,
						Assigner assigner,
						Advice.ArgumentHandler.ForInstrumentedMethod argumentHandler,
						Advice.ArgumentHandler.ForInstrumentedMethod methodSizeHandler,
						Advice.ArgumentHandler.ForInstrumentedMethod stackMapFrameHandler,
						TypeDescription instrumentedType,
						MethodDescription instrumentedMethod,
						Advice.Dispatcher.SuppressionHandler.Bound suppressionHandler,
						Advice.Dispatcher.RelocationHandler.Bound relocationHandler,
						StackManipulation exceptionHandler);


				protected class AdviceMethodInliner extends ClassVisitor implements Advice.Dispatcher.Bound {
					private final TypeDescription instrumentedType;
					private final MethodDescription instrumentedMethod;
					private final MethodVisitor methodVisitor;
					private final Implementation.Context implementationContext;
					private final Assigner assigner;
					private final Advice.ArgumentHandler.ForInstrumentedMethod argumentHandler;
					private final Advice.ArgumentHandler.ForInstrumentedMethod methodSizeHandler;
					private final Advice.ArgumentHandler.ForInstrumentedMethod stackMapFrameHandler;
					private final Advice.Dispatcher.SuppressionHandler.Bound suppressionHandler;
					private final Advice.Dispatcher.RelocationHandler.Bound relocationHandler;
					private final StackManipulation exceptionHandler;
					private final ClassReader classReader;
					private final List<Label> labels = new ArrayList<>();

					public AdviceMethodInliner(ClassReader classReader, StackManipulation exceptionHandler, Advice.Dispatcher.RelocationHandler.Bound relocationHandler, Advice.Dispatcher.SuppressionHandler.Bound suppressionHandler, Advice.ArgumentHandler.ForInstrumentedMethod stackMapFrameHandler, Advice.ArgumentHandler.ForInstrumentedMethod methodSizeHandler, Advice.ArgumentHandler.ForInstrumentedMethod argumentHandler, Assigner assigner, Implementation.Context implementationContext, MethodVisitor methodVisitor, MethodDescription instrumentedMethod, TypeDescription instrumentedType) {
						super(OpenedClassReader.ASM_API);
						this.classReader = classReader;
						this.exceptionHandler = exceptionHandler;
						this.relocationHandler = relocationHandler;
						this.suppressionHandler = suppressionHandler;
						this.stackMapFrameHandler = stackMapFrameHandler;
						this.methodSizeHandler = methodSizeHandler;
						this.argumentHandler = argumentHandler;
						this.assigner = assigner;
						this.implementationContext = implementationContext;
						this.methodVisitor = methodVisitor;
						this.instrumentedMethod = instrumentedMethod;
						this.instrumentedType = instrumentedType;
					}

					@Override
					public void prepare() {
						classReader.accept(new ExceptionTableExtractor(), ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
						suppressionHandler.onPrepare(methodVisitor);
					}

					@Override
					public void initialize() {
						for (Map.Entry<Integer, TypeDefinition> entry : resolveInitializationTypes(argumentHandler).entrySet()) {
							if (entry.getValue().represents(boolean.class)
									|| entry.getValue().represents(byte.class)
									|| entry.getValue().represents(short.class)
									|| entry.getValue().represents(char.class)
									|| entry.getValue().represents(int.class)) {
								methodVisitor.visitInsn(Opcodes.ICONST_0);
								methodVisitor.visitVarInsn(Opcodes.ISTORE, entry.getKey());
							}
							else if (entry.getValue().represents(long.class)) {
								methodVisitor.visitInsn(Opcodes.LCONST_0);
								methodVisitor.visitVarInsn(Opcodes.LSTORE, entry.getKey());
							}
							else if (entry.getValue().represents(float.class)) {
								methodVisitor.visitInsn(Opcodes.FCONST_0);
								methodVisitor.visitVarInsn(Opcodes.FSTORE, entry.getKey());
							}
							else if (entry.getValue().represents(double.class)) {
								methodVisitor.visitInsn(Opcodes.DCONST_0);
								methodVisitor.visitVarInsn(Opcodes.DSTORE, entry.getKey());
							}
							else {
								methodVisitor.visitInsn(Opcodes.ACONST_NULL);
								methodVisitor.visitVarInsn(Opcodes.ASTORE, entry.getKey());
							}
							methodSizeHandler.requireStackSize(entry.getValue().getStackSize().getSize());
						}
					}

					@Override
					public void apply() {
						classReader.accept(this, ClassReader.SKIP_DEBUG | stackMapFrameHandler.getReaderHit());
					}

					@Override
					public MethodVisitor visitMethod(int modifiers, String internalName, String descriptor, String signature, String[] exceptions) {
						return adviceMethod.getInternalName().equals(internalName) && adviceMethod.getDescriptor().equals(descriptor)
								? new ExceptionTableSubstitutor(Advice.Dispatcher.Inlining.Resolved.this.apply(methodVisitor, implementationContext, assigner, argumentHandler, methodSizeHandler,
								stackMapFrameHandler, instrumentedType, instrumentedMethod, suppressionHandler,
								relocationHandler, exceptionHandler))
								: IGNORE_METHOD;
					}

					protected class ExceptionTableExtractor extends ClassVisitor {

						protected ExceptionTableExtractor() {
							super(OpenedClassReader.ASM_API);
						}

						@Override
						public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
							return adviceMethod.getInternalName().equals(name) && adviceMethod.getDescriptor().equals(descriptor)
									? new ExceptionTableCollector(methodVisitor)
									: IGNORE_METHOD;
						}
					}

					protected class ExceptionTableCollector extends MethodVisitor {
						private final MethodVisitor methodVisitor;

						public ExceptionTableCollector(MethodVisitor methodVisitor) {
							super(OpenedClassReader.ASM_API);
							this.methodVisitor = methodVisitor;
						}

						@Override
						public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
							methodVisitor.visitTryCatchBlock(start, end, handler, type);
							labels.addAll(Arrays.asList(start, end, handler));
						}

						@Override
						public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
							return methodVisitor.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
						}
					}

					protected class ExceptionTableSubstitutor extends MethodVisitor {
						private Map<Label, Label> substitutions;
						private int index;

						protected ExceptionTableSubstitutor(MethodVisitor methodVisitor) {
							super(OpenedClassReader.ASM_API, methodVisitor);
							substitutions = new IdentityHashMap<>();
						}

						@Override
						public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
							substitutions.put(start, labels.get(index++));
							substitutions.put(end, labels.get(index++));
							Label actual = labels.get(index++);
							substitutions.put(handler, actual);
							((CodeTranslationVisitor) mv).propagateHandler(actual);
						}

						@Override
						public AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
							return IGNORE_ANNOTATION;
						}

						@Override
						public void visitLabel(Label label) {
							super.visitLabel(resolve(label));
						}

						@Override
						public void visitJumpInsn(int opcode, Label label) {
							super.visitJumpInsn(opcode, resolve(label));
						}

						@Override
						public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
							super.visitTableSwitchInsn(min, max, dflt, resolve(labels));
						}

						@Override
						public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
							super.visitLookupSwitchInsn(dflt, keys, resolve(labels));
						}

						private Label resolve(Label label) {
							Label sub = substitutions.get(label);
							return sub == null ? label : sub;
						}

						private Label[] resolve(Label... labels) {
							return IntStream.range(0, labels.length).mapToObj(i -> resolve(labels[i])).toArray(Label[]::new);
						}
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				protected abstract static class ForMethodEnter extends Advice.Dispatcher.Inlining.Resolved implements AgentAdvice.Dispatcher.Resolved.ForMethodEnter {
					private final Map<String, TypeDefinition> namedTypes;
					private final boolean prependLineNumber;

					public ForMethodEnter(MethodDescription.InDefinedShape adviceMethod,
							Advice.PostProcessor postProcessor,
							Map<String, TypeDefinition> namedTypes,
							List<? extends Advice.OffsetMapping.Factory<?>> factories,
							TypeDescription exitType,
							ClassReader classReader) {
						super(adviceMethod, postProcessor,
								CompoundList.of(Arrays.asList(
										Advice.OffsetMapping.ForArgument.Unresolved.Factory.INSTANCE,
										Advice.OffsetMapping.ForAllArguments.Factory.INSTANCE,

										)));
						this.namedTypes = namedTypes;
						this.prependLineNumber = prependLineNumber;
					}
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class Delegating implements Unresolved {

			protected final MethodDescription.InDefinedShape adviceMethod;
			protected final Advice.Delegator delegator;

			@Override
			public boolean isAlive() {
				return true;
			}

			@Override
			public boolean isBinary() {
				return false;
			}

			@Override
			public TypeDefinition getAdviceType() {
				return adviceMethod.getReturnType().asErasure();
			}

			@Override
			public Map<String, TypeDefinition> getNamedTypes() {
				return Map.of();
			}

			@Override
			public Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodExit, PostProcessor.Factory postProcessorFactory) {
				return Resolved.ForMethodEnter.of(adviceMethod, postProcessorFactory.make(adviceMethod, false), delegator, userFactories, methodExit.getAdviceType(), methodExit.isAlive());
			}

			@Override
			public Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories, ClassReader classReader, Unresolved methodEnter, PostProcessor.Factory postProcessorFactory) {
				Map<String, TypeDefinition> namedTypes = methodEnter.getNamedTypes();
				for (ParameterDescription.InDefinedShape parameterDescription : adviceMethod.getParameters().filter(isAnnotatedWith(Local.class))) {
					String name = parameterDescription.getDeclaredAnnotations()
							.ofType(Local.class)
							.getValue(ForLocalValue.Factory.LOCAL_VALUE)
							.resolve(String.class);
					TypeDefinition typeDefinition = namedTypes.get(name);
					if (typeDefinition == null) {
						throw new IllegalStateException(adviceMethod + " attempts use of undeclared local variable " + name);
					}
					else if (!typeDefinition.equals(parameterDescription.getType())) {
						throw new IllegalStateException(adviceMethod + " does not read variable " + name + " as " + typeDefinition);
					}
				}
				return Resolved.ForMethodExit.of(adviceMethod, postProcessorFactory.make(adviceMethod, true), delegator, namedTypes, userFactories, methodEnter.getAdviceType());
			}

			protected abstract static class Resolved extends Dispatcher.Resolved.AbstractBase {

				protected final Delegator delegator;

				public Resolved(InDefinedShape adviceMethod, PostProcessor postProcessor, List<? extends com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> factories, TypeDescription throwableType, TypeDescription relocatableType, Delegator delegator) {
					super(adviceMethod, postProcessor, factories, throwableType, relocatableType, OffsetMapping.Factory.AdviceType.DELEGATION);
					this.delegator = delegator;
				}

				@Override
				public Map<String, TypeDefinition> getNamedTypes() {
					return Map.of();
				}

				@Override
				public Bound bind(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Context implementationContext, Assigner assigner,
						com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForInstrumentedMethod argumentHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
						StackManipulation exceptionHandler, RelocationHandler.Relocation relocation) {
					if (!adviceMethod.isVisibleTo(instrumentedType)) {
						throw new IllegalStateException(adviceMethod + " is not visible to " + instrumentedMethod.getDeclaringType());
					}
					return resolve(instrumentedType, instrumentedMethod, methodVisitor,
							implementationContext, assigner, argumentHandler, methodSizeHandler, stackMapFrameHandler,
							exceptionHandler, relocation);
				}

				protected abstract Bound resolve(TypeDescription type,
						MethodDescription method,
						MethodVisitor methodVisitor,
						Implementation.Context implementationContext,
						Assigner assigner,
						com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForInstrumentedMethod argumentHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
						com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
						StackManipulation exceptionHandler,
						RelocationHandler.Relocation relocation
				);

				@AllArgsConstructor
				@HashCodeAndEqualsPlugin.Enhance
				protected abstract static class AdviceMethodWriter implements Bound {
					protected final MethodDescription.InDefinedShape adviceMethod;
					private final TypeDescription instrumentedType;
					private final MethodDescription instrumentedMethod;
					private final Assigner assigner;
					private final List<com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Target> offsetMappings;
					protected final MethodVisitor methodVisitor;
					private final Implementation.Context implementationContext;
					protected final com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForAdvice argumentHandler;
					protected final com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler;
					private final com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler;
					private final SuppressionHandler.Bound suppressionHandler;
					private final RelocationHandler.Bound relocationHandler;
					private final StackManipulation exceptionHandler;
					private final PostProcessor postProcessor;
					private final Delegator delegator;

					@Override
					public void prepare() {
						suppressionHandler.onPrepare(methodVisitor);
					}

					@Override
					public void apply() {
						suppressionHandler.onStart(methodVisitor);
						int index = 0, currentStackSize = 0, maximumStackSize = 0;
						for (com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Target offsetMapping : offsetMappings) {
							currentStackSize += adviceMethod.getParameters().get(index++).getType().getStackSize().getSize();
							maximumStackSize = Math.max(maximumStackSize, currentStackSize + offsetMapping.resolveRead()
									.apply(methodVisitor, implementationContext)
									.getMaximalSize());
						}
						delegator.apply(methodVisitor, adviceMethod, instrumentedType, instrumentedMethod, isExitAdvice());
						suppressionHandler.onEndWithSkip(methodVisitor, implementationContext, methodSizeHandler, stackMapFrameHandler, adviceMethod.getReturnType());
						TypeDescription.Generic returnType = adviceMethod.getReturnType();

						IntSupplier handlerSupplier = () -> isExitAdvice() ? argumentHandler.exit() : argumentHandler.enter();
						IntSupplier opcodesSupplier = null;
						if (returnType.represents(boolean.class)
								|| returnType.represents(byte.class)
								|| returnType.represents(short.class)
								|| returnType.represents(char.class)
								|| returnType.represents(int.class)) {
							opcodesSupplier = () -> Opcodes.ISTORE;
						}
						else if (returnType.represents(long.class)) {
							opcodesSupplier = () -> Opcodes.LSTORE;
						}
						else if (returnType.represents(float.class)) {
							opcodesSupplier = () -> Opcodes.FSTORE;
						}
						else if (returnType.represents(double.class)) {
							opcodesSupplier = () -> Opcodes.DSTORE;
						}
						else if (returnType.represents(void.class)) {
							opcodesSupplier = () -> Opcodes.ASTORE;
						}
						if (opcodesSupplier != null) {
							methodVisitor.visitVarInsn(opcodesSupplier.getAsInt(), handlerSupplier.getAsInt());
						}
						methodSizeHandler.requireStackSize(postProcessor.resolve(instrumentedType,
										instrumentedMethod, assigner, argumentHandler, stackMapFrameHandler, exceptionHandler)
								.apply(methodVisitor, implementationContext).getMaximalSize());
						methodSizeHandler.requireStackSize(relocationHandler.apply(methodVisitor, handlerSupplier.getAsInt()));
						stackMapFrameHandler.injectCompletionFrame(methodVisitor);
						methodSizeHandler.requireStackSize(Math.max(maximumStackSize, adviceMethod.getReturnType().getStackSize().getSize()));
						methodSizeHandler.requireLocalVariableLength(instrumentedMethod.getStackSize() + adviceMethod.getReturnType().getStackSize().getSize());
					}

					protected abstract boolean isExitAdvice();

					protected static class ForMethodEnter extends AdviceMethodWriter {
						public ForMethodEnter(InDefinedShape adviceMethod, TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, List<com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Target> offsetMappings, MethodVisitor methodVisitor, Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForAdvice argumentHandler, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, SuppressionHandler.Bound suppressionHandler, RelocationHandler.Bound relocationHandler, StackManipulation exceptionHandler, PostProcessor postProcessor, Delegator delegator) {
							super(adviceMethod, instrumentedType, instrumentedMethod, assigner, offsetMappings, methodVisitor, implementationContext, argumentHandler, methodSizeHandler, stackMapFrameHandler, suppressionHandler, relocationHandler, exceptionHandler, postProcessor, delegator);
						}

						@Override
						public void initialize() {
							// ignored
						}

						@Override
						protected boolean isExitAdvice() {
							return false;
						}
					}

					protected static class ForMethodExit extends AdviceMethodWriter {

						public ForMethodExit(InDefinedShape adviceMethod, TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, List<com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Target> offsetMappings, MethodVisitor methodVisitor, Context implementationContext, com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForAdvice argumentHandler, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForAdvice methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForAdvice stackMapFrameHandler, SuppressionHandler.Bound suppressionHandler, RelocationHandler.Bound relocationHandler, StackManipulation exceptionHandler, PostProcessor postProcessor, Delegator delegator) {
							super(adviceMethod, instrumentedType, instrumentedMethod, assigner, offsetMappings, methodVisitor, implementationContext, argumentHandler, methodSizeHandler, stackMapFrameHandler, suppressionHandler, relocationHandler, exceptionHandler, postProcessor, delegator);
						}

						@Override
						public void initialize() {
							IntSupplier opcodesSupplier = null;
							IntSupplier opcodesVarSupplier = null;
							TypeDescription.Generic returnType = adviceMethod.getReturnType();
							if (returnType.represents(boolean.class)
									|| returnType.represents(byte.class)
									|| returnType.represents(short.class)
									|| returnType.represents(char.class)
									|| returnType.represents(int.class)) {
								opcodesSupplier = () -> Opcodes.ICONST_0;
								opcodesVarSupplier = () -> Opcodes.ISTORE;
							}
							else if (returnType.represents(long.class)) {
								opcodesSupplier = () -> Opcodes.LCONST_0;
								opcodesVarSupplier = () -> Opcodes.LSTORE;
							}
							else if (returnType.represents(float.class)) {
								opcodesSupplier = () -> Opcodes.FCONST_0;
								opcodesVarSupplier = () -> Opcodes.FSTORE;
							}
							else if (returnType.represents(double.class)) {
								opcodesSupplier = () -> Opcodes.DCONST_0;
								opcodesVarSupplier = () -> Opcodes.DSTORE;
							}
							else if (returnType.represents(void.class)) {
								opcodesSupplier = () -> Opcodes.ACONST_NULL;
								opcodesVarSupplier = () -> Opcodes.ASTORE;
							}
							if (opcodesSupplier != null && opcodesVarSupplier != null) {
								methodVisitor.visitInsn(opcodesSupplier.getAsInt());
								methodVisitor.visitVarInsn(opcodesVarSupplier.getAsInt(), argumentHandler.exit());
							}
							methodSizeHandler.requireStackSize(adviceMethod.getReturnType().getStackSize().getSize());
						}

						@Override
						protected boolean isExitAdvice() {
							return true;
						}
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				protected abstract static class ForMethodEnter extends Delegating.Resolved implements Dispatcher.Resolved.ForMethodExit {
					private final boolean prependLineNumber;

					protected ForMethodEnter(MethodDescription.InDefinedShape adviceMethod,
							PostProcessor postProcessor,
							List<? extends com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> userFactories,
							TypeDefinition exitType,
							Delegator delegator) {
						super(adviceMethod,
								postProcessor,
								CompoundList.of(List.of(
										ForArgument.Unresolved.Factory.INSTANCE,
										ForAllArguments.Factory.INSTANCE,
										ForThisReference.Factory.INSTANCE,
										ForOrigin.Factory.INSTANCE,
										ForUnusedValue.Factory.INSTANCE,
										ForStubValue.INSTANCE,
										ForExitValue.Factory.of(exitType),
										new Illegal<>(Thrown.class),
										new Illegal<>(Enter.class),
										new Illegal<>(Local.class),
										new Illegal<>(Return.class)), userFactories),
								adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(SUPPRESS_ENTER).resolve(TypeDescription.class),
								adviceMethod.getDeclaredAnnotations().ofType(OnMethodExit.class).getValue(SKIP_ON).resolve(TypeDescription.class),
								delegator
						);
						prependLineNumber = adviceMethod.getDeclaredAnnotations().ofType(OnMethodEnter.class).getValue(PREPEND_LINE_NUMBER).resolve(Boolean.class);
					}

					protected static Resolved.ForMethodEnter of(MethodDescription.InDefinedShape adviceMethod,
							PostProcessor postProcessor,
							Delegator delegator,
							List<? extends com.mawen.agent.core.plugin.transformer.advice.support.OffsetMapping.Factory<?>> userFactories,
							TypeDefinition exitType,
							boolean methodExit) {
						return methodExit
								? new WithRetainedEnterType();
								:new;
					}

					public boolean isPrependLineNumber() {
						return prependLineNumber;
					}

					public TypeDefinition getActualAdviceType() {
						return adviceMethod.getReturnType();
					}

					@Override
					protected Bound resolve(TypeDescription type, MethodDescription method, MethodVisitor methodVisitor, Context implementationContext, Assigner assigner, com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForInstrumentedMethod argumentHandler, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler, StackManipulation exceptionHandler, RelocationHandler.Relocation relocation) {
						return doResolve(type, method, methodVisitor, implementationContext, assigner, argumentHandler, methodSizeHandler, stackMapFrameHandler, exceptionHandler, relocation);
					}

					protected Bound doResolve(TypeDescription type, MethodDescription method, MethodVisitor methodVisitor, Context implementationContext, Assigner assigner, com.mawen.agent.core.plugin.transformer.advice.support.ArgumentHandler.ForInstrumentedMethod argumentHandler, com.mawen.agent.core.plugin.transformer.advice.support.MethodSizeHandler.ForInstrumentedMethod methodSizeHandler, com.mawen.agent.core.plugin.transformer.advice.support.StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler, StackManipulation exceptionHandler, RelocationHandler.Relocation relocation) {

					}
				}
			}

		}
	}


	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@java.lang.annotation.Target(ElementType.METHOD)
	public @interface OnMethodExitNoException {
		Class<?> repeatOn() default void.class;

		Class<? extends Throwable> onThrowable() default NoExceptionHandler.class;

		boolean backupArguments() default true;

		boolean inline() default true;

		Class<? extends Throwable> suppress() default NoExceptionHandler.class;
	}

	public static class NoExceptionHandler extends Throwable {
		private static final long serialVersionUID = 1L;
		private static final TypeDescription DESCRIPTION = TypeDescription.ForLoadedType.of(NoExceptionHandler.class);

		private NoExceptionHandler() {
			throw new UnsupportedOperationException("This class only serves as a marker type and should not be instantiated");
		}

	}
}