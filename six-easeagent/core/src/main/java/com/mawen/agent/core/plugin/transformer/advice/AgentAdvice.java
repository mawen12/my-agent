package com.mawen.agent.core.plugin.transformer.advice;

import java.io.IOException;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

import com.mawen.agent.core.plugin.registry.AdviceRegistry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.build.HashCodeAndEqualsPlugin;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.enumeration.EnumerationDescription;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.SuperMethodCall;
import net.bytebuddy.implementation.bytecode.Addition;
import net.bytebuddy.implementation.bytecode.Duplication;
import net.bytebuddy.implementation.bytecode.Removal;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.DefaultValue;
import net.bytebuddy.implementation.bytecode.constant.DoubleConstant;
import net.bytebuddy.implementation.bytecode.constant.FloatConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.implementation.bytecode.constant.LongConstant;
import net.bytebuddy.implementation.bytecode.constant.MethodConstant;
import net.bytebuddy.implementation.bytecode.constant.NullConstant;
import net.bytebuddy.implementation.bytecode.constant.SerializedConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.AnnotationVisitor;
import net.bytebuddy.jar.asm.ClassReader;
import net.bytebuddy.jar.asm.MethodVisitor;
import net.bytebuddy.utility.JavaConstant;
import net.bytebuddy.utility.JavaType;
import net.bytebuddy.utility.OpenedClassReader;

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
	private static final InDefinedShape ON_THROWABLE;
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

	private static boolean isNoExceptionHandler(TypeDescription t) {
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
				} else {
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
		} else {
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
		} else {
			exit = methodExit;
		}

		if (AdviceRegistry.check(instrumentedType, instrumentedMethod, methodEnter, exit) == 0) {

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

	public interface Dispatcher {
		MethodVisitor IGNORE_METHOD = null;
		AnnotationVisitor IGNORE_ANNOTATION = null;

		boolean isAlive();

		TypeDefinition getAdviceType();

		interface Unresolved extends Dispatcher {
			boolean isBinary();

			Map<String, TypeDefinition> getNamedTypes();

			Resolved.ForMethodEnter asMethodEnter(List<? extends OffsetMapping.Factory<?>> userFactories,
					ClassReader classReader,
					Unresolved methodExit,
					PostProcessor.Factory postProcessorFactory);

			Resolved.ForMethodExit asMethodExit(List<? extends OffsetMapping.Factory<?>> userFactories,
					ClassReader classReader,
					Unresolved methodEnter,
					PostProcessor.Factory postProcessorFactory);
		}

		interface Resolved extends Dispatcher {
			Map<String, TypeDefinition> getNamedTypes();

			Bound bind(TypeDescription instrumentedType,
					MethodDescription instrumentedMethod,
					MethodVisitor methodVisitor,
					Implementation.Context implementationContext,
					Assigner assigner,
					ArgumentHandler.ForInstrumentedMethod argumentHandler,
					MethodSizeHandler.ForInstrumentedMethod methodSizeHandler,
					StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler,
					Advice.Dispatcher.RelocationHandler.Relocation relocation);

			Map<Integer, OffsetMapping> getOffsetMapping();

			interface ForMethodEnter extends Resolved {
				boolean isPrependLineNumber();

				TypeDefinition getActualAdviceType();
			}

			interface ForMethodExit extends Resolved {
				TypeDescription getThrowable();

				ArgumentHandler.Factory getArgumentHandlerFactory();
			}

			@HashCodeAndEqualsPlugin.Enhance
			abstract class AbstractBase implements Resolved {
				protected final MethodDescription.InDefinedShape adviceMethod;
				protected final PostProcessor postProcessor;
				protected final Map<Integer, OffsetMapping> offsetMappings;
				protected final Advice.Dispatcher.SuppressionHandler suppressionHandler;
				protected final Advice.Dispatcher.RelocationHandler relocationHandler;

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
			public Bound bind(TypeDescription instrumentedType, MethodDescription instrumentedMethod, MethodVisitor methodVisitor, Context implementationContext, Assigner assigner, ArgumentHandler.ForInstrumentedMethod argumentHandler, MethodSizeHandler.ForInstrumentedMethod methodSizeHandler, StackMapFrameHandler.ForInstrumentedMethod stackMapFrameHandler, Advice.Dispatcher.RelocationHandler.Relocation relocation) {
				return this;
			}

			@Override
			public Map<Integer, OffsetMapping> getOffsetMapping() {
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
				return NoExceptionHandler.DESCRIPTION;
			}

			@Override
			public ArgumentHandler.Factory getArgumentHandlerFactory() {
				return ArgumentHandler.Factory.SIMPLE;
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

			protected Inlining(InDefinedShape adviceMethod) {
				this.adviceMethod = adviceMethod;
				this.namedTypes = new HashMap<>();
				for (ParameterDescription.InDefinedShape parameterDescription : adviceMethod.getParameters().filter(isAnnotatedWith(Local.class))) {
					parameterDescription.getDeclaredAnnotations()
							.ofType(Local.class).getValue(OffsetMapping.ForLocalValue.Factory.LOCAL)
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
		}
	}

	public interface ArgumentHandler {
		int THIS_REFERENCE = 0;

		int argument(int offset);

		int exit();

		int enter();

		int named(String name);

		int returned();

		int thrown();

		interface ForInstrumentedMethod extends ArgumentHandler {

		}

		interface ForAdvice extends ArgumentHandler, Advice.ArgumentHandler {

		}

		enum Factory {
			SIMPLE{
				@Override
				protected ForInstrumentedMethod resolve(MethodDescription instrumentMethod, TypeDefinition enterType, TypeDescription exitType, SortedMap<String, TypeDefinition> namedTypes) {

				}
			},
			COPYING{
				@Override
				protected ForInstrumentedMethod resolve(MethodDescription instrumentMethod, TypeDefinition enterType, TypeDescription exitType, SortedMap<String, TypeDefinition> namedTypes) {

				}
			},
			;

			protected abstract ForInstrumentedMethod resolve(MethodDescription instrumentMethod,
					TypeDefinition enterType,
					TypeDescription exitType,
					SortedMap<String, TypeDefinition> namedTypes);
		}
	}

	public interface OffsetMapping {

		Target resolve(TypeDescription instrumentedType,
				MethodDescription instrumentedMethod,
				Assigner assigner,
				ArgumentHandler argumentHandler,
				Sort sort);

		interface Target {
			StackManipulation resolveRead();

			StackManipulation resolveWrite();

			StackManipulation resolveIncrement(int value);

			abstract class AbstractReadOnlyAdapter implements Target {
				@Override
				public StackManipulation resolveWrite() {
					throw new IllegalStateException("Cannot write to read-only value");
				}

				@Override
				public StackManipulation resolveIncrement(int value) {
					throw new IllegalStateException("Cannot write to read-only value");
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			abstract class ForDefaultValue implements Target {
				protected final TypeDefinition typeDefinition;
				protected final StackManipulation readAssignment;

				@Override
				public StackManipulation resolveRead() {
					return new StackManipulation.Compound(DefaultValue.of(typeDefinition), readAssignment);
				}

				public static class ReadOnly extends ForDefaultValue {
					public ReadOnly(TypeDefinition typeDefinition) {
						this(typeDefinition, StackManipulation.Trivial.INSTANCE);
					}

					protected ReadOnly(TypeDefinition typeDefinition, StackManipulation readAssignment) {
						super(typeDefinition, readAssignment);
					}

					@Override
					public StackManipulation resolveWrite() {
						throw new IllegalStateException("Cannot write to read-only default value");
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						throw new IllegalStateException("Cannot write to read-only default value");
					}
				}

				public static class ReadWrite extends ForDefaultValue {
					public ReadWrite(TypeDefinition typeDefinition) {
						this(typeDefinition, StackManipulation.Trivial.INSTANCE);
					}

					public ReadWrite(TypeDefinition typeDefinition, StackManipulation readAssignment) {
						super(typeDefinition, readAssignment);
					}

					@Override
					public StackManipulation resolveWrite() {
						return Removal.of(typeDefinition);
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						return StackManipulation.Trivial.INSTANCE;
					}
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			abstract class ForVariable implements Target {
				protected final TypeDefinition typeDefinition;
				protected final int offset;
				protected final StackManipulation readAssignment;

				@Override
				public StackManipulation resolveRead() {
					return new StackManipulation.Compound(MethodVariableAccess.of(typeDefinition).loadFrom(offset), readAssignment);
				}

				public static class ReadOnly extends ForVariable {
					public ReadOnly(TypeDefinition typeDefinition, int offset) {
						this(typeDefinition, offset, StackManipulation.Trivial.INSTANCE);
					}

					public ReadOnly(TypeDefinition typeDefinition, int offset, StackManipulation readAssignment) {
						super(typeDefinition, offset, readAssignment);
					}

					@Override
					public StackManipulation resolveWrite() {
						throw new IllegalStateException("Cannot write to read-only parameter " + typeDefinition + " at " + offset);
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						throw new IllegalStateException("Cannot write to read-only parameter " + typeDefinition + " at " + offset);
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				public static class ReadWrite extends ForVariable {
					private final StackManipulation writeAssignment;

					public ReadWrite(TypeDefinition typeDefinition, int offset) {
						this(typeDefinition, offset, StackManipulation.Trivial.INSTANCE, StackManipulation.Trivial.INSTANCE);
					}

					public ReadWrite(TypeDefinition typeDefinition, int offset, StackManipulation readAssignment, StackManipulation writeAssignment) {
						super(typeDefinition, offset, readAssignment);
						this.writeAssignment = writeAssignment;
					}

					@Override
					public StackManipulation resolveWrite() {
						return new StackManipulation.Compound(writeAssignment, MethodVariableAccess.of(typeDefinition).storeAt(offset));
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						return typeDefinition.represents(int.class)
								? MethodVariableAccess.of(typeDefinition).increment(offset, value)
								: new StackManipulation.Compound(resolveRead(), IntegerConstant.forValue(1), Addition.INTEGER, resolveWrite());
					}
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			abstract class ForArray implements Target {
				protected final TypeDescription.Generic target;
				protected final List<? extends StackManipulation> valueReads;

				@Override
				public StackManipulation resolveRead() {
					return ArrayFactory.forType(target).withValues(valueReads);
				}

				@Override
				public StackManipulation resolveIncrement(int value) {
					throw new IllegalStateException("Cannot increment read-only array value");
				}

				public static class ReadOnly extends ForArray {
					public ReadOnly(TypeDescription.Generic target, List<? extends StackManipulation> valueReads) {
						super(target, valueReads);
					}

					@Override
					public StackManipulation resolveWrite() {
						throw new IllegalStateException("Cannot write to read-only array value");
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				public static class ReadWrite extends ForArray {

					private final List<? extends StackManipulation> valueWrites;

					public ReadWrite(TypeDescription.Generic target, List<? extends StackManipulation> valueReads, List<? extends StackManipulation> valueWrites) {
						super(target, valueReads);
						this.valueWrites = valueWrites;
					}

					@Override
					public StackManipulation resolveWrite() {
						return new StackManipulation.Compound(ArrayAccess.of(target).forEach(valueWrites));
					}
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			abstract class ForField implements Target {
				protected final FieldDescription fieldDescription;
				protected final StackManipulation readAssignment;

				@Override
				public StackManipulation resolveRead() {
					return new StackManipulation.Compound(fieldDescription.isStatic()
							? StackManipulation.Trivial.INSTANCE
							: MethodVariableAccess.loadThis(), FieldAccess.forField(fieldDescription).read(), readAssignment);
				}

				public static class ReadOnly extends ForField {

					public ReadOnly(FieldDescription fieldDescription) {
						this(fieldDescription, StackManipulation.Trivial.INSTANCE);
					}

					public ReadOnly(FieldDescription fieldDescription, StackManipulation readAssignment) {
						super(fieldDescription, readAssignment);
					}

					@Override
					public StackManipulation resolveWrite() {
						throw new IllegalStateException("Cannot write to read-only field value");
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						throw new IllegalStateException("Cannot write to read-only field value");
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				public static class ReadWrite extends ForField {
					private final StackManipulation writeAssignment;

					public ReadWrite(FieldDescription fieldDescription) {
						this(fieldDescription, StackManipulation.Trivial.INSTANCE, StackManipulation.Trivial.INSTANCE);
					}

					public ReadWrite(FieldDescription fieldDescription, StackManipulation readAssignment, StackManipulation writeAssignment) {
						super(fieldDescription, readAssignment);
						this.writeAssignment = writeAssignment;
					}

					@Override
					public StackManipulation resolveWrite() {
						StackManipulation preparation;
						if (fieldDescription.isStatic()) {
							preparation = StackManipulation.Trivial.INSTANCE;
						}
						else {
							preparation = new StackManipulation.Compound(
									MethodVariableAccess.loadThis(),
									Duplication.SINGLE.flipOver(fieldDescription.getType()),
									Removal.SINGLE);
						}
						return new StackManipulation.Compound(writeAssignment, preparation, FieldAccess.forField(fieldDescription).write());
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						return new StackManipulation.Compound(
								resolveRead(),
								IntegerConstant.forValue(value),
								Addition.INTEGER,
								resolveWrite()
						);
					}
				}
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			class ForStackManipulation implements Target {
				private final StackManipulation stackManipulation;

				public static Target of(MethodDescription.InDefinedShape methodDescription) {
					return new ForStackManipulation(MethodConstant.of(methodDescription));
				}

				public static Target of(TypeDescription typeDescription) {
					return new ForStackManipulation(ClassConstant.of(typeDescription));
				}

				public static Target of(Object value) {
					if (value == null) {
						return new ForStackManipulation(NullConstant.INSTANCE);
					}
					else if (value instanceof Boolean b) {
						return new ForStackManipulation(IntegerConstant.forValue(b));
					}
					else if (value instanceof Byte b) {
						return new ForStackManipulation(IntegerConstant.forValue(b));
					}
					else if (value instanceof Short s) {
						return new ForStackManipulation(IntegerConstant.forValue(s));
					}
					else if (value instanceof Character c) {
						return new ForStackManipulation(IntegerConstant.forValue(c));
					}
					else if (value instanceof Integer i) {
						return new ForStackManipulation(IntegerConstant.forValue(i));
					}
					else if (value instanceof Long l) {
						return new ForStackManipulation(LongConstant.forValue(l));
					}
					else if (value instanceof Float f) {
						return new ForStackManipulation(FloatConstant.forValue(f));
					}
					else if (value instanceof Double d) {
						return new ForStackManipulation(DoubleConstant.forValue(d));
					}
					else if (value instanceof String s) {
						return new ForStackManipulation(new TextConstant(s));
					}
					else if (value instanceof Enum<?> e) {
						return new ForStackManipulation(
								FieldAccess.forEnumeration(new EnumerationDescription.ForLoadedEnumeration(e)));
					}
					else if (value instanceof Class<?> c) {
						return new ForStackManipulation(ClassConstant.of(TypeDescription.ForLoadedType.of(c)));
					}
					else if (value instanceof TypeDescription t) {
						return new ForStackManipulation(ClassConstant.of(t));
					}
					else if (JavaType.METHOD_HANDLE.isInstance(value)) {
						return new ForStackManipulation(new JavaConstantValue(JavaConstant.MethodHandle.ofLoaded(value)));
					}
					else if (JavaType.METHOD_TYPE.isInstance(value)) {
						return new ForStackManipulation(new JavaConstantValue(JavaConstant.MethodType.ofLoaded(value)));
					}
					else if (value instanceof JavaConstant j) {
						return new ForStackManipulation(new JavaConstantValue(j));
					}
					else {
						throw new IllegalArgumentException("Not a constant value: " + value);
					}
				}

				@Override
				public StackManipulation resolveRead() {
					return stackManipulation;
				}

				@Override
				public StackManipulation resolveWrite() {
					throw new IllegalStateException("Cannot write to constant value: " + stackManipulation);
				}

				@Override
				public StackManipulation resolveIncrement(int value) {
					throw new IllegalStateException("Cannot write to constant value: " + stackManipulation);
				}

				@AllArgsConstructor
				@HashCodeAndEqualsPlugin.Enhance
				public static class Writable implements Target {
					private final StackManipulation read;
					private final StackManipulation write;

					@Override
					public StackManipulation resolveRead() {
						return read;
					}

					@Override
					public StackManipulation resolveWrite() {
						return write;
					}

					@Override
					public StackManipulation resolveIncrement(int value) {
						throw new IllegalStateException("Cannot increment mutable constant value: " + write);
					}
				}
			}
		}

		interface Factory<T extends Annotation> {

			Class<T> getAnnotationType();

			OffsetMapping make(ParameterDescription.InDefinedShape target,
					AnnotationDescription.Loadable<T> annotation, AdviceType adviceType);

			@Getter
			@AllArgsConstructor
			enum AdviceType {
				DELEGATION(true),
				INLINING(false),
				;
				private final boolean delegation;
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			class Simple<T extends Annotation> implements Factory<T> {
				private final Class<T> annotationType;
				private final OffsetMapping offsetMapping;

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return offsetMapping;
				}
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			class Illegal<T extends Annotation> implements Factory<T> {
				private final Class<T> annotationType;

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					throw new IllegalStateException("Usage of " + annotationType + " is not allowed on " + target);
				}
			}
		}

		enum Sort {
			ENTER {
				@Override
				public boolean isPremature(MethodDescription methodDescription) {
					return methodDescription.isConstructor();
				}
			},
			EXIT {
				@Override
				public boolean isPremature(MethodDescription methodDescription) {
					return false;
				}
			},
			;

			public abstract boolean isPremature(MethodDescription methodDescription);
		}

		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForArgument implements OffsetMapping {

		}

		@HashCodeAndEqualsPlugin.Enhance
		class ForThisReference implements OffsetMapping {

		}

		@HashCodeAndEqualsPlugin.Enhance
		class ForAllArguments implements OffsetMapping {

		}

		class ForInstrumentedTYpe implements OffsetMapping {

		}

		class ForInstrumentedMethod implements OffsetMapping {

		}

		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForField implements OffsetMapping {

		}

		@HashCodeAndEqualsPlugin.Enhance
		class ForOrigin implements OffsetMapping {

		}

		@HashCodeAndEqualsPlugin.Enhance
		class ForUnusedValue implements OffsetMapping {

		}

		enum ForStubValue implements OffsetMapping, Factory<StubValue> {

		}


		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForEnterValue implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final TypeDescription.Generic enterType;
			private final boolean readOnly;
			private final Assigner.Typing typing;

			protected ForEnterValue(TypeDescription.Generic target, TypeDescription.Generic enterType, AnnotationDescription.Loadable<Enter> annotation) {
				this(target, enterType,
						annotation.getValue(Factory.ENTER_READ_ONLY).resolve(Boolean.class),
						annotation.getValue(Factory.ENTER_TYPING).load(Enter.class.getClassLoader()).resolve(Assigner.Typing.class));
			}

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation readAssignment = assigner.assign(enterType, target, typing);
				if (!readAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + enterType + " to " + target);
				}
				else if (readOnly) {
					return new Target.ForVariable.ReadOnly(target, argumentHandler.enter(), readAssignment);
				}
				else {
					StackManipulation writeAssignment = assigner.assign(target, enterType, typing);
					if (!writeAssignment.isValid()) {
						throw new IllegalStateException("Cannot assign " + target + " to " + enterType);
					}
					return new Target.ForVariable.ReadWrite(target, argumentHandler.enter(), readAssignment, writeAssignment);
				}
				return null;
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			protected static class Factory implements OffsetMapping.Factory<Enter> {
				private static final MethodDescription.InDefinedShape ENTER_READ_ONLY;
				private static final MethodDescription.InDefinedShape ENTER_TYPING;

				static {
					MethodList<InDefinedShape> methods = TypeDescription.ForLoadedType.of(Enter.class).getDeclaredMethods();
					ENTER_READ_ONLY = methods.filter(named("readOnly")).getOnly();
					ENTER_TYPING = methods.filter(named("typing")).getOnly();
				}

				private final TypeDefinition enterType;

				protected static OffsetMapping.Factory<Enter> of(TypeDefinition typeDefinition) {
					return typeDefinition.represents(void.class)
							? new Illegal<>(Enter.class)
							: new Factory(typeDefinition);
				}

				@Override
				public Class<Enter> getAnnotationType() {
					return Enter.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Enter> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(ENTER_READ_ONLY).resolve(Boolean.class)) {
						throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
					} else {
						return new ForEnterValue(target.getType(), enterType.asGenericType(), annotation);
					}
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForExitValue implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final TypeDescription.Generic exitType;
			private final boolean readOnly;
			private final Assigner.Typing typing;

			protected ForExitValue(TypeDescription.Generic target, TypeDescription.Generic exitType, AnnotationDescription.Loadable<Exit> annotation) {
				this(target, exitType,
						annotation.getValue(Factory.EXIT_READ_ONLY).resolve(Boolean.class),
						annotation.getValue(Factory.EXIT_TYPING).load(Exit.class.getClassLoader()).resolve(Assigner.Typing.class));
			}

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation readAssignment = assigner.assign(exitType, target, typing);
				if (!readAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + exitType + " to " + target);
				}
				else if (readOnly) {
					return new Target.ForVariable.ReadOnly(target, argumentHandler.exit(), readAssignment);
				}
				else {
					StackManipulation writeAssignment = assigner.assign(target, exitType, typing);
					if (!writeAssignment.isValid()) {
						throw new IllegalStateException("Cannot assign " + target + " to " + exitType);
					}
					return new Target.ForVariable.ReadWrite(target, argumentHandler.exit(), readAssignment, writeAssignment);
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			protected static class Factory implements OffsetMapping.Factory<Exit> {
				private static final MethodDescription.InDefinedShape EXIT_READ_ONLY;
				private static final MethodDescription.InDefinedShape EXIT_TYPING;

				static {
					MethodList<InDefinedShape> methods = TypeDescription.ForLoadedType.of(Exit.class).getDeclaredMethods();
					EXIT_READ_ONLY = methods.filter(named("typing")).getOnly();
					EXIT_TYPING = methods.filter(named("typing")).getOnly();
				}

				private final TypeDefinition exitType;

				protected static OffsetMapping.Factory<Exit> of(TypeDefinition typeDefinition) {
					return typeDefinition.represents(void.class)
							? new Illegal<>(Exit.class)
							: new Factory(typeDefinition);
				}

				@Override
				public Class<Exit> getAnnotationType() {
					return Exit.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Exit> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(EXIT_READ_ONLY).resolve(Boolean.class)) {
						throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
					}
					else {
						return new ForExitValue(target.getType(), exitType.asGenericType(), annotation);
					}
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForLocalValue implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final TypeDescription.Generic localType;
			private final String name;

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation readAssignment = assigner.assign(localType, target, Assigner.Typing.STATIC);
				StackManipulation writeAssignment = assigner.assign(target, localType, Assigner.Typing.STATIC);
				if (!readAssignment.isValid() || !writeAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + localType + " to " + target);
				}
				else {
					return new Target.ForVariable.ReadWrite(target, argumentHandler.named(name), readAssignment, writeAssignment);
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			protected static class Factory implements OffsetMapping.Factory<Local> {
				protected static final MethodDescription.InDefinedShape LOCAL_VALUE = TypeDescription.ForLoadedType.of(Local.class)
						.getDeclaredMethods()
						.filter(named("value"))
						.getOnly();
				private final Map<String, TypeDefinition> namedTypes;

				@Override
				public Class<Local> getAnnotationType() {
					return Local.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Local> annotation, AdviceType adviceType) {
					String name = annotation.getValue(LOCAL_VALUE).resolve(String.class);
					TypeDefinition namedType = namedTypes.get(name);
					if (namedType == null) {
						throw new IllegalStateException("Named local variable is unknown: " + name);
					}
					return new ForLocalValue(target.getType(), namedType.asGenericType(), name);
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForReturnValue implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final boolean readOnly;
			private final Assigner.Typing typing;

			protected ForReturnValue(TypeDescription.Generic target, AnnotationDescription.Loadable<Return> annotation) {
				this(target,
						annotation.getValue(Factory.RETURN_READ_ONLY).resolve(Boolean.class),
						annotation.getValue(Factory.RETURN_TYPING).load(Return.class.getClassLoader()).resolve(Assigner.Typing.class));
			}

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation readAssignment = assigner.assign(instrumentedMethod.getReturnType(), target, typing);
				if (!readAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + instrumentedMethod.getReturnType() + " to " + target);
				}
				else if (readOnly) {
					return instrumentedMethod.getReturnType().represents(Void.class)
							? new Target.ForDefaultValue.ReadOnly(target)
							: new Target.ForVariable.ReadOnly(instrumentedMethod.getReturnType(), argumentHandler.returned(), readAssignment);
				}
				else {
					StackManipulation writeAssignment = assigner.assign(target, instrumentedMethod.getReturnType(), typing);
					if (!writeAssignment.isValid()) {
						throw new IllegalStateException("Cannot assign " + target + " to " + instrumentedMethod.getReturnType());
					}
					return instrumentedMethod.getReturnType().represents(Void.class)
							? new Target.ForDefaultValue.ReadWrite(target)
							: new Target.ForVariable.ReadWrite(instrumentedMethod.getReturnType(), argumentHandler.returned(), readAssignment, writeAssignment);
				}
			}

			protected enum Factory implements OffsetMapping.Factory<Return> {
				INSTANCE;

				private static final MethodDescription.InDefinedShape RETURN_READ_ONLY;
				private static final MethodDescription.InDefinedShape RETURN_TYPING;

				static {
					MethodList<InDefinedShape> methods = TypeDescription.ForLoadedType.of(Return.class).getDeclaredMethods();
					RETURN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
					RETURN_TYPING = methods.filter(named("typing")).getOnly();
				}

				@Override
				public Class<Return> getAnnotationType() {
					return Return.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Return> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(RETURN_READ_ONLY).resolve(Boolean.class)) {
						throw new IllegalStateException("Cannot write return value for " + target + " in read-only context");
					}
					else {
						return new ForReturnValue(target.getType(), annotation);
					}
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForThrowable implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final boolean readOnly;
			private final Assigner.Typing typing;

			protected ForThrowable(TypeDescription.Generic target, AnnotationDescription.Loadable<Thrown> annotation) {
				this(target,
						annotation.getValue(Factory.THROWN_READ_ONLY).resolve(Boolean.class),
						annotation.getValue(Factory.THROWN_TYPING).load(Thrown.class.getClassLoader()).resolve(Assigner.Typing.class));
			}

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation readAssignment = assigner.assign(TypeDescription.THROWABLE.asGenericType(), target, typing);
				if (!readAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign Throwable to " + target);
				}
				else if (readOnly) {
					return new Target.ForVariable.ReadOnly(TypeDescription.THROWABLE, argumentHandler.thrown(), readAssignment);
				}
				else {
					StackManipulation writeAssignment = assigner.assign(target, TypeDescription.THROWABLE.asGenericType().asGenericType(), typing);
					if (!writeAssignment.isValid()) {
						throw new IllegalStateException("Cannot assign " + target + " to Throwable");
					}
					return new Target.ForVariable.ReadWrite(TypeDescription.THROWABLE, argumentHandler.thrown(), readAssignment, writeAssignment);
				}
			}

			protected enum Factory implements OffsetMapping.Factory<Thrown> {
				INSTANCE;

				private static final MethodDescription.InDefinedShape THROWN_READ_ONLY;

				private static final MethodDescription.InDefinedShape THROWN_TYPING;

				static {
					MethodList<InDefinedShape> methods = TypeDescription.ForLoadedType.of(Thrown.class).getDeclaredMethods();
					THROWN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
					THROWN_TYPING = methods.filter(named("typing")).getOnly();
				}

				protected static OffsetMapping.Factory<?> of(MethodDescription.InDefinedShape adviceMethod) {
					return isNoExceptionHandler(adviceMethod.getDeclaredAnnotations()
							.ofType(OnMethodExit.class)
							.getValue(ON_THROWABLE)
							.resolve(TypeDescription.class))
							? new OffsetMapping.Factory.Illegal<>(Thrown.class) : Factory.INSTANCE;
				}

				@Override
				public Class<Thrown> getAnnotationType() {
					return Thrown.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Thrown> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(THROWN_READ_ONLY).resolve(Boolean.class)) {
						throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
					}
					else {
						return new ForThrowable(target.getType(), annotation);
					}
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForStackManipulation implements OffsetMapping {
			@Getter
			private final StackManipulation stackManipulation;
			private final TypeDescription.Generic typeDescription;
			private final TypeDescription.Generic targetType;
			private final Assigner.Typing typing;

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation assignment = assigner.assign(typeDescription, targetType, typing);
				if (!assignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + typeDescription + " to " + targetType);
				}
				return new Target.ForStackManipulation(new StackManipulation.Compound(stackManipulation, assignment));
			}

			public ForStackManipulation with(StackManipulation stackManipulation) {
				return new ForStackManipulation(stackManipulation, this.typeDescription, this.targetType, this.typing);
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;
				private final StackManipulation stackManipulation;
				private final TypeDescription.Generic typeDescription;

				public Factory(Class<T> annotationType, TypeDescription typeDescription) {
					this(annotationType, ClassConstant.of(typeDescription), TypeDescription.CLASS.asGenericType());
				}

				public Factory(Class<T> annotationType, EnumerationDescription enumerationDescription) {
					this(annotationType, FieldAccess.forEnumeration(enumerationDescription), enumerationDescription.getEnumerationType().asGenericType());
				}

				public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType, Object value) {
					StackManipulation stackManipulation;
					TypeDescription typeDescription;
					if (value == null) {
						return new OfDefaultValue<>(annotationType);
					}
					else if (value instanceof Boolean b) {
						stackManipulation = IntegerConstant.forValue(b);
						typeDescription = TypeDescription.ForLoadedType.of(boolean.class);
					}
					else if (value instanceof Byte b) {
						stackManipulation = IntegerConstant.forValue(b);
						typeDescription = TypeDescription.ForLoadedType.of(byte.class);
					}
					else if (value instanceof Short s) {
						stackManipulation = IntegerConstant.forValue(s);
						typeDescription = TypeDescription.ForLoadedType.of(short.class);
					}
					else if (value instanceof Character c) {
						stackManipulation = IntegerConstant.forValue(c);
						typeDescription = TypeDescription.ForLoadedType.of(char.class);
					}
					else if (value instanceof Integer i) {
						stackManipulation = IntegerConstant.forValue(i);
						typeDescription = TypeDescription.ForLoadedType.of(int.class);
					}
					else if (value instanceof Long l) {
						stackManipulation = LongConstant.forValue(l);
						typeDescription = TypeDescription.ForLoadedType.of(long.class);
					}
					else if (value instanceof Float f) {
						stackManipulation = FloatConstant.forValue(f);
						typeDescription = TypeDescription.ForLoadedType.of(float.class);
					}
					else if (value instanceof Double d) {
						stackManipulation = DoubleConstant.forValue(d);
						typeDescription = TypeDescription.ForLoadedType.of(double.class);
					}
					else if (value instanceof String s) {
						stackManipulation = new TextConstant(s);
						typeDescription = TypeDescription.ForLoadedType.of(String.class);
					}
					else if (value instanceof Class<?> c) {
						stackManipulation = ClassConstant.of(TypeDescription.ForLoadedType.of(c));
						typeDescription = TypeDescription.ForLoadedType.of(Class.class);
					}
					else if (value instanceof TypeDescription t) {
						stackManipulation = ClassConstant.of(t);
						typeDescription = TypeDescription.ForLoadedType.of(TypeDescription.class);
					}
					else if (value instanceof Enum<?> e) {
						stackManipulation = FieldAccess.forEnumeration(new EnumerationDescription.ForLoadedEnumeration(e));
						typeDescription = TypeDescription.ForLoadedType.of(e.getDeclaringClass());
					}
					else if (value instanceof EnumerationDescription e) {
						stackManipulation = FieldAccess.forEnumeration(e);
						typeDescription = e.getEnumerationType();
					}
					else if (JavaType.METHOD_HANDLE.isInstance(value)) {
						JavaConstant constant = JavaConstant.MethodHandle.ofLoaded(value);
						stackManipulation = new JavaConstantValue(constant);
						typeDescription = constant.getTypeDescription();
					}
					else if (JavaType.METHOD_TYPE.isInstance(value)) {
						JavaConstant constant = JavaConstant.MethodType.ofLoaded(value);
						stackManipulation = new JavaConstantValue(constant);
						typeDescription = constant.getTypeDescription();
					}
					else if (value instanceof JavaConstant j) {
						stackManipulation = new JavaConstantValue(j);
						typeDescription = j.getTypeDescription();
					}
					else {
						throw new IllegalStateException("Not a constant value: " + value);
					}
					return new Factory<>(annotationType, stackManipulation, typeDescription.asGenericType());
				}

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return new ForStackManipulation(stackManipulation, typeDescription, target.getType(), Assigner.Typing.STATIC);
				}
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			public static class OfDefaultValue<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return new ForStackManipulation(DefaultValue.of(target.getType()), target.getType(), target.getType(), Assigner.Typing.STATIC);
				}
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			public static class OfAnnotationProperty<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;
				private final MethodDescription.InDefinedShape property;

				public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType, String property) {
					if (!annotationType.isAnnotation()) {
						throw new IllegalArgumentException("Not an annotation type: " + annotationType);
					}
					try {
						return new OfAnnotationProperty<>(annotationType, new ForLoadedMethod(annotationType.getMethod(property)));
					}
					catch (NoSuchMethodException e) {
						throw new IllegalArgumentException("Cannot find a property " + property + " on " + annotationType, e);
					}
				}

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					Object value = annotation.getValue(property).resolve();
					OffsetMapping.Factory<T> factory;
					if (value instanceof TypeDescription t) {
						factory = new Factory<>(annotationType, t);
					}
					else if (value instanceof EnumerationDescription e) {
						factory = new Factory<>(annotationType, e);
					}
					else if (value instanceof AnnotationDescription a) {
						throw new IllegalStateException("Cannot bind annotation as fixed value for " + property);
					}
					else {
						factory = Factory.of(annotationType, value);
					}
					return factory.make(target, annotation, adviceType);
				}
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			public static class OfDynamicInvocation<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;
				private final MethodDescription.InDefinedShape bootstrapMethod;
				private final List<? extends JavaConstant> arguments;

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					if (!target.getType().isInterface()) {
						throw new IllegalArgumentException(target.getType() + " is not an interface");
					}
					else if (!target.getType().getInterfaces().isEmpty()) {
						throw new IllegalArgumentException(target.getType() + " must not extend other interfaces");
					}
					else if (!target.getType().isPublic()) {
						throw new IllegalArgumentException(target.getType() + " is not public");
					}

					MethodList<InGenericShape> methodCandidates = target.getType().getDeclaredMethods().filter(isAbstract());
					if (methodCandidates.size() != 1) {
						throw new IllegalArgumentException(target.getType() + " must declare exactly one abstract method");
					}
					return new ForStackManipulation(
							MethodInvocation.invoke(bootstrapMethod).dynamic(methodCandidates.getOnly().getInternalName(),
									target.getType().asErasure(),
									methodCandidates.getOnly().getParameters().asTypeList().asErasures(),
									arguments), target.getType(), target.getType(), Assigner.Typing.STATIC);
				}
			}
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		class ForSerializedValue implements OffsetMapping {
			private final TypeDescription.Generic target;
			private final TypeDescription typeDescription;
			private final StackManipulation deserialization;

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				StackManipulation assignment = assigner.assign(typeDescription.asGenericType(), target, Assigner.Typing.DYNAMIC);
				if (!assignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + typeDescription + " to " + target);
				}
				return new Target.ForStackManipulation(new StackManipulation.Compound(deserialization, assignment));
			}

			@AllArgsConstructor(access = AccessLevel.PROTECTED)
			@HashCodeAndEqualsPlugin.Enhance
			public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;
				private final TypeDescription typeDescription;
				private final StackManipulation deserialization;

				public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType,
						Serializable target, Class<?> targetType) {
					if (!targetType.isInstance(targetType)) {
						throw new IllegalArgumentException(target + " is no instance of " + targetType);
					}
					return new Factory<>(annotationType, TypeDescription.ForLoadedType.of(targetType), SerializedConstant.of(target));
				}

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return new ForSerializedValue(target.getType(), typeDescription, deserialization);
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
