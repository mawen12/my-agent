package com.mawen.agent.core.plugin.transformer.advice.support;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mawen.agent.core.plugin.transformer.advice.AgentAdvice;
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
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.TypeDefinition;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.TargetType;
import net.bytebuddy.dynamic.scaffold.FieldLocator;
import net.bytebuddy.implementation.FieldAccessor;
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
import net.bytebuddy.utility.JavaConstant;
import net.bytebuddy.utility.JavaType;

import static com.mawen.agent.core.plugin.transformer.advice.AgentAdvice.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/7
 */
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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForDefaultValue implements Target {
			public final TypeDefinition typeDefinition;
			public final StackManipulation readAssignment;

			@Override
			public StackManipulation resolveRead() {
				return new StackManipulation.Compound(DefaultValue.of(typeDefinition), readAssignment);
			}

			public static class ReadOnly extends ForDefaultValue {
				public ReadOnly(TypeDefinition typeDefinition) {
					this(typeDefinition, StackManipulation.Trivial.INSTANCE);
				}

				public ReadOnly(TypeDefinition typeDefinition, StackManipulation readAssignment) {
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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForVariable implements Target {
			public final TypeDefinition typeDefinition;
			public final int offset;
			public final StackManipulation readAssignment;

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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForArray implements Target {
			public final TypeDescription.Generic target;
			public final List<? extends StackManipulation> valueReads;

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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		abstract class ForField implements Target {
			public final FieldDescription fieldDescription;
			public final StackManipulation readAssignment;

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

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	abstract class ForArgument implements OffsetMapping {
		public final TypeDescription.Generic target;
		public final boolean readOnly;
		private final Assigner.Typing typing;

		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			ParameterDescription parameterDescription = resolve(instrumentedMethod);
			StackManipulation read = assigner.assign(parameterDescription.getType(), target, typing);
			if (!read.isValid()) {
				throw new IllegalStateException("Cannot assign " + parameterDescription + " to " + target);
			}
			else if (readOnly) {
				return new Target.ForVariable.ReadOnly(parameterDescription.getType(), argumentHandler.argument(parameterDescription.getOffset()), read);
			}
			else {
				StackManipulation write = assigner.assign(target, parameterDescription.getType(), typing);
				if (!write.isValid()) {
					throw new IllegalStateException("Cannot assign " + parameterDescription + " to " + target);
				}
				return new Target.ForVariable.ReadWrite(parameterDescription.getType(), argumentHandler.argument(parameterDescription.getOffset()), read, write);
			}
		}

		public abstract ParameterDescription resolve(MethodDescription methodDescription);

		@HashCodeAndEqualsPlugin.Enhance
		public static class Unresolved extends ForArgument {
			private final int index;
			private final boolean optional;

			public Unresolved(TypeDescription.Generic target, AnnotationDescription.Loadable<Argument> annotation) {
				this(target,
						annotation.getValue(Factory.ARGUMENT_READ_ONLY).resolve(Boolean.class),
						annotation.getValue(Factory.ARGUMENT_TYPING).load(Argument.class.getClassLoader()).resolve(Assigner.Typing.class),
						annotation.getValue(Factory.ARGUMENT_VALUE).resolve(Integer.class),
						annotation.getValue(Factory.ARGUMENT_OPTIONAL).resolve(Boolean.class));
			}

			public Unresolved(ParameterDescription parameterDescription) {
				this(parameterDescription.getType(), true, Assigner.Typing.STATIC, parameterDescription.getIndex());
			}

			public Unresolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, int index) {
				this(target, readOnly, typing, index, false);
			}

			public Unresolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, int index, boolean optional) {
				super(target, readOnly, typing);
				this.index = index;
				this.optional = optional;
			}

			@Override
			public ParameterDescription resolve(MethodDescription methodDescription) {
				ParameterList<?> parameters = methodDescription.getParameters();
				if (parameters.size() < index) {
					throw new IllegalStateException(methodDescription + " does not define an index " + index);
				} else {
					return parameters.get(index);
				}
			}

			@Override
			public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
				if (optional && instrumentedMethod.getParameters().size() <= index) {
					return readOnly
							? new Target.ForDefaultValue.ReadOnly(target)
							: new Target.ForDefaultValue.ReadWrite(target);
				}
				return super.resolve(instrumentedType, instrumentedMethod, assigner, argumentHandler, sort);
			}

			public enum Factory implements OffsetMapping.Factory<Argument> {
				INSTANCE;

				private static final MethodDescription.InDefinedShape ARGUMENT_VALUE;
				private static final MethodDescription.InDefinedShape ARGUMENT_READ_ONLY;
				private static final MethodDescription.InDefinedShape ARGUMENT_TYPING;
				private static final MethodDescription.InDefinedShape ARGUMENT_OPTIONAL;

				static {
					MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Argument.class).getDeclaredMethods();
					ARGUMENT_VALUE = methods.filter(named("value")).getOnly();
					ARGUMENT_READ_ONLY = methods.filter(named("readOnly")).getOnly();
					ARGUMENT_TYPING = methods.filter(named("typing")).getOnly();
					ARGUMENT_OPTIONAL = methods.filter(named("optional")).getOnly();
				}

				@Override
				public Class<Argument> getAnnotationType() {
					return Argument.class;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Argument> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(ARGUMENT_READ_ONLY).resolve(boolean.class)) {
						throw new IllegalStateException("Cannot define writable field access for "
								+ target + " when using delegation");
					}
					return new ForArgument.Unresolved(target.getType(), annotation);
				}
			}
		}

		@HashCodeAndEqualsPlugin.Enhance
		public static class Resolved extends ForArgument {
			private final ParameterDescription parameterDescription;

			public Resolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, ParameterDescription parameterDescription) {
				super(target, readOnly, typing);
				this.parameterDescription = parameterDescription;
			}

			@Override
			public ParameterDescription resolve(MethodDescription methodDescription) {
				if (!parameterDescription.getDeclaringMethod().equals(methodDescription)) {
					throw new IllegalStateException(parameterDescription + " is not a parameter of " + methodDescription);
				}
				return parameterDescription;
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;;
				private final ParameterDescription parameterDescription;
				private final boolean readOnly;
				private final Assigner.Typing typing;

				public Factory(Class<T> annotationType, ParameterDescription parameterDescription) {
					this(annotationType, parameterDescription, true, Assigner.Typing.STATIC);
				}

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return new Resolved(target.getType(), readOnly, typing, parameterDescription);
				}
			}
		}
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	class ForThisReference implements OffsetMapping {
		private final TypeDescription.Generic target;
		private final boolean readOnly;
		private final Assigner.Typing typing;
		private final boolean optional;

		public ForThisReference(TypeDescription.Generic target, AnnotationDescription.Loadable<Advice.This> annotation) {
			this(target,
					annotation.getValue(Factory.THIS_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.THIS_TYPING).load(Advice.This.class.getClassLoader()).resolve(Assigner.Typing.class),
					annotation.getValue(Factory.THIS_OPTIONAL).resolve(Boolean.class));
		}

		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			if (instrumentedMethod.isStatic() || sort.isPremature(instrumentedMethod)) {
				if (optional) {
					return readOnly
							? new Target.ForDefaultValue.ReadOnly(instrumentedType)
							: new Target.ForDefaultValue.ReadWrite(instrumentedType);
				}
				else {
					throw new IllegalStateException(
							"Cannot map this reference for static method or constructor start: " + instrumentedMethod);
				}
			}
			StackManipulation readAssignment = assigner.assign(instrumentedType.asGenericType(), target, typing);
			if (!readAssignment.isValid()) {
				throw new IllegalStateException("Cannot assign " + instrumentedType + " to " + target);
			}
			else if (readOnly) {
				return new Target.ForVariable.ReadOnly(instrumentedType.asGenericType(),
						argumentHandler.argument(ArgumentHandler.THIS_REFERENCE), readAssignment);
			}
			else {
				StackManipulation writeAssignment = assigner.assign(target, instrumentedType.asGenericType(), typing);
				if (!writeAssignment.isValid()) {
					throw new IllegalStateException("Cannot assign " + target + " to " + instrumentedType);
				}
				return new Target.ForVariable.ReadWrite(instrumentedType.asGenericType(),
						argumentHandler.argument(ArgumentHandler.THIS_REFERENCE), readAssignment, writeAssignment);
			}
		}

		public enum Factory implements OffsetMapping.Factory<Advice.This> {
			INSTANCE;

			private static final MethodDescription.InDefinedShape THIS_READ_ONLY;
			private static final MethodDescription.InDefinedShape THIS_TYPING;
			private static final MethodDescription.InDefinedShape THIS_OPTIONAL;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.This.class).getDeclaredMethods();
				THIS_READ_ONLY = methods.filter(named("readOnly")).getOnly();
				THIS_TYPING = methods.filter(named("typing")).getOnly();
				THIS_OPTIONAL = methods.filter(named("optional")).getOnly();
			}

			@Override
			public Class<Advice.This> getAnnotationType() {
				return Advice.This.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.This> annotation, AdviceType adviceType) {
				if (adviceType.isDelegation() && !annotation.getValue(THIS_READ_ONLY).resolve(Boolean.class)) {
					throw new IllegalStateException("Cannot write to this reference for " + target + " in read-only context");
				}
				else {
					return new ForThisReference(target.getType(), annotation);
				}
			}
		}
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	class ForAllArguments implements OffsetMapping {
		private final TypeDescription.Generic target;
		private final boolean readOnly;
		private final Assigner.Typing typing;
		private final boolean nullIfEmpty;

		public ForAllArguments(TypeDescription.Generic target,
				AnnotationDescription.Loadable<Advice.AllArguments> annotation) {
			this(target,
					annotation.getValue(Factory.ALL_ARGUMENTS_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.ALL_ARGUMENTS_TYPING).load(Advice.AllArguments.class.getClassLoader()).resolve(Assigner.Typing.class),
					annotation.getValue(Factory.ALL_ARGUMENTS_NULL_IF_EMPTY).resolve(Boolean.class)
			);
		}

		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			if (nullIfEmpty && instrumentedMethod.getParameters().isEmpty()) {
				return readOnly
						? new Target.ForStackManipulation(NullConstant.INSTANCE)
						: new Target.ForStackManipulation.Writable(NullConstant.INSTANCE, Removal.SINGLE);
			}
			List<StackManipulation> valueReads = new ArrayList<>(instrumentedMethod.getParameters().size());
			for (ParameterDescription parameter : instrumentedMethod.getParameters()) {
				StackManipulation read = assigner.assign(parameter.getType(), target, typing);
				if (!read.isValid()) {
					throw new IllegalStateException("Cannot assign " + parameter + " to " + target);
				}
				valueReads.add(new StackManipulation.Compound(MethodVariableAccess.of(parameter.getType())
						.loadFrom(argumentHandler.argument(parameter.getOffset())), read));
			}
			if (readOnly) {
				return new Target.ForArray.ReadOnly(target, valueReads);
			}
			else {
				List<StackManipulation> valueWrites = new ArrayList<>(instrumentedMethod.getParameters().size());
				for (ParameterDescription parameter : instrumentedMethod.getParameters()) {
					StackManipulation write = assigner.assign(target, parameter.getType(), typing);
					if (!write.isValid()) {
						throw new IllegalStateException("Cannot assign " + target + " to " + parameter);
					}
					valueWrites.add(new StackManipulation.Compound(write,
							MethodVariableAccess.of(parameter.getType())
									.storeAt(argumentHandler.argument(parameter.getOffset()))));
				}
				return new Target.ForArray.ReadWrite(target, valueReads, valueWrites);
			}
		}

		public enum Factory implements OffsetMapping.Factory<Advice.AllArguments> {
			INSTANCE;

			private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_READ_ONLY;
			private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_TYPING;
			private static final MethodDescription.InDefinedShape ALL_ARGUMENTS_NULL_IF_EMPTY;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.AllArguments.class).getDeclaredMethods();
				ALL_ARGUMENTS_READ_ONLY = methods.filter(named("readOnly")).getOnly();
				ALL_ARGUMENTS_TYPING = methods.filter(named("typing")).getOnly();
				ALL_ARGUMENTS_NULL_IF_EMPTY = methods.filter(named("nullIfEmpty")).getOnly();
			}

			@Override
			public Class<Advice.AllArguments> getAnnotationType() {
				return Advice.AllArguments.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.AllArguments> annotation, AdviceType adviceType) {
				if (!target.getType().represents(Object.class) && !target.getType().isArray()) {
					throw new IllegalStateException("Cannot use AllArguments annotation on a non-array type");
				}
				else if (adviceType.isDelegation() && !annotation.getValue(ALL_ARGUMENTS_READ_ONLY).resolve(Boolean.class)) {
					throw new IllegalStateException("Cannot define writable field access for " + target);
				}
				else {
					return new ForAllArguments(target.getType().represents(Object.class)
							? TypeDescription.Generic.OBJECT
							: target.getType().getComponentType(), annotation);
				}
			}
		}

	}

	enum ForInstrumentedType implements OffsetMapping {
		INSTANCE;


		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			return Target.ForStackManipulation.of(instrumentedType);
		}
	}

	enum ForInstrumentedMethod implements OffsetMapping {
		METHOD {
			@Override
			public boolean isRepresentable(MethodDescription methodDescription) {
				return methodDescription.isMethod();
			}
		},
		CONSTRUCTOR {
			@Override
			public boolean isRepresentable(MethodDescription methodDescription) {
				return methodDescription.isConstructor();
			}
		},
		EXECUTABLE {
			@Override
			public boolean isRepresentable(MethodDescription methodDescription) {
				return true;
			}
		};

		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			if (!isRepresentable(instrumentedMethod)) {
				throw new IllegalStateException("Cannot represent " + instrumentedMethod + " as given method constant");
			}
			return Target.ForStackManipulation.of(instrumentedMethod.asDefined());
		}

		public abstract boolean isRepresentable(MethodDescription methodDescription);
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	abstract class ForField implements OffsetMapping {
		private static final MethodDescription.InDefinedShape VALUE;
		private static final MethodDescription.InDefinedShape DECLARING_VALUE;
		private static final MethodDescription.InDefinedShape READ_ONLY;
		private static final MethodDescription.InDefinedShape TYPING;

		static {
			MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.FieldValue.class).getDeclaredMethods();
			VALUE = methods.filter(named("value")).getOnly();
			DECLARING_VALUE = methods.filter(named("declaringValue")).getOnly();
			READ_ONLY = methods.filter(named("readOnly")).getOnly();
			TYPING = methods.filter(named("typing")).getOnly();
		}

		private final TypeDescription.Generic target;
		private final boolean readOnly;
		private final Assigner.Typing typing;

		@Override
		public Target resolve(TypeDescription type, MethodDescription method, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			FieldDescription field = resolve(type, method);
			if (!field.isStatic() && method.isStatic()) {
				throw new IllegalStateException("Cannot read non-static field " + field + " from static method " + method);
			}
			else if (sort.isPremature(method) && !field.isStatic()) {
				throw new IllegalStateException("Cannot access non-static field before calling constructor: " + method);
			}
			StackManipulation read = assigner.assign(field.getType(), target, typing);
			if (!read.isValid()) {
				throw new IllegalStateException("Cannot assign " + field + " to " + target);
			}
			else if (readOnly) {
				return new Target.ForField.ReadOnly(field, read);
			}
			else {
				StackManipulation write = assigner.assign(target, field.getType(), typing);
				if (!write.isValid()) {
					throw new IllegalStateException("Cannot assign " + target + " to " + field);
				}
				return new Target.ForField.ReadWrite(field.asDefined(), read, write);
			}
		}

		public abstract FieldDescription resolve(TypeDescription typeDescription, MethodDescription methodDescription);

		@HashCodeAndEqualsPlugin.Enhance
		public abstract static class Unresolved extends ForField {
			public static final String BEAN_PROPERTY = "";
			private final String name;

			public Unresolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, String name) {
				super(target, readOnly, typing);
				this.name = name;
			}

			@Override
			public FieldDescription resolve(TypeDescription typeDescription, MethodDescription methodDescription) {
				FieldLocator locator = fieldLocator(typeDescription);
				FieldLocator.Resolution resolution = name.equals(BEAN_PROPERTY)
						? resolveAccessor(locator, methodDescription)
						: locator.locate(name);
				if (!resolution.isResolved()) {
					throw new IllegalStateException("Cannot locate field named " + name + " for " + typeDescription);
				}
				else {
					return resolution.getField();
				}
			}

			private static FieldLocator.Resolution resolveAccessor(FieldLocator fieldLocator, MethodDescription methodDescription) {
				String fieldName;
				if (isSetter().matches(methodDescription)) {
					fieldName = methodDescription.getInternalName().substring(3);
				}
				else if (isGetter().matches(methodDescription)) {
					fieldName = methodDescription.getInternalName().substring(methodDescription.getInternalName().startsWith("is") ? 2 : 3);
				}
				else {
					return FieldLocator.Resolution.Illegal.INSTANCE;
				}
				return fieldLocator.locate(Character.toLowerCase(fieldName.charAt(0)) + fieldName.substring(1));
			}

			public abstract FieldLocator fieldLocator(TypeDescription typeDescription);

			public enum Factory implements OffsetMapping.Factory<Advice.FieldValue> {
				INSTANCE;

				@Override
				public Class<Advice.FieldValue> getAnnotationType() {
					return Advice.FieldValue.class;
				}

				public static class WithImplicitType extends Unresolved {
					public WithImplicitType(TypeDescription.Generic target, AnnotationDescription.Loadable<Advice.FieldValue> annotation) {
						this(target,
								annotation.getValue(READ_ONLY).resolve(Boolean.class),
								annotation.getValue(TYPING).load(Assigner.Typing.class.getClassLoader()).resolve(Assigner.Typing.class),
								annotation.getValue(VALUE).resolve(String.class)
						);
					}

					public WithImplicitType(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, String name) {
						super(target, readOnly, typing, name);
					}

					@Override
					public FieldLocator fieldLocator(TypeDescription typeDescription) {
						return new FieldLocator.ForClassHierarchy(typeDescription);
					}
				}

				@HashCodeAndEqualsPlugin.Enhance
				public static class WithExplicitType extends Unresolved {
					private final TypeDescription declaringType;

					public WithExplicitType(TypeDescription.Generic target,
							AnnotationDescription.Loadable<Advice.FieldValue> annotation,
							TypeDescription declaringType) {
						this(target,
								annotation.getValue(READ_ONLY).resolve(Boolean.class),
								annotation.getValue(TYPING).load(Assigner.Typing.class.getClassLoader()).resolve(Assigner.Typing.class),
								annotation.getValue(VALUE).resolve(String.class),
								declaringType);

					}

					public WithExplicitType(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, String name, TypeDescription declaringType) {
						super(target, readOnly, typing, name);
						this.declaringType = declaringType;
					}

					@Override
					public FieldLocator fieldLocator(TypeDescription typeDescription) {
						if (!declaringType.represents(TargetType.class) && !typeDescription.isAssignableTo(declaringType)) {
							throw new IllegalStateException(declaringType + " is no super type of " + typeDescription);
						}
						return new FieldLocator.ForExactType(TargetType.resolve(declaringType, typeDescription));
					}
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.FieldValue> annotation, AdviceType adviceType) {
					if (adviceType.isDelegation() && !annotation.getValue(ForField.READ_ONLY).resolve(Boolean.class)) {
						throw new IllegalStateException("Cannot write to field for " + target + " in read-only context");
					}
					else {
						TypeDescription declaringType = annotation.getValue(DECLARING_VALUE).resolve(TypeDescription.class);
						return declaringType.represents(void.class)
								? new WithImplicitType(target.getType(), annotation)
								: new WithExplicitType(target.getType(), annotation, declaringType);
					}
				}
			}
		}

		@HashCodeAndEqualsPlugin.Enhance
		public static class Resolved extends ForField {
			private final FieldDescription field;

			public Resolved(TypeDescription.Generic target, boolean readOnly, Assigner.Typing typing, FieldDescription field) {
				super(target, readOnly, typing);
				this.field = field;
			}

			@Override
			public FieldDescription resolve(TypeDescription type, MethodDescription method) {
				if (!method.isStatic() && !field.getDeclaringType().asErasure().isAssignableFrom(type)) {
					throw new IllegalStateException(field + " is no member of " + type);
				}
				else if (!field.isAccessibleTo(type)) {
					throw new IllegalStateException("Cannot access " + field + " from " + type);
				}
				return field;
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			public static class Factory<T extends Annotation> implements OffsetMapping.Factory<T> {
				private final Class<T> annotationType;
				private final FieldDescription field;
				private final boolean readOnly;
				private final Assigner.Typing typing;

				public Factory(Class<T> annotationType, FieldDescription field) {
					this(annotationType, field, true, Assigner.Typing.STATIC);
				}

				@Override
				public Class<T> getAnnotationType() {
					return annotationType;
				}

				@Override
				public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<T> annotation, AdviceType adviceType) {
					return new Resolved(target.getType(), readOnly, typing, field);
				}
			}
		}
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	class ForOrigin implements OffsetMapping {
		private static final char DELIMITER = '#';
		private static final char ESCAPE = '\\';

		private final List<Renderer> renderers;

		public static OffsetMapping parse(String pattern) {
			if (pattern.equals(Advice.Origin.DEFAULT)) {
				return new ForOrigin(Collections.singletonList(Renderer.ForStringRepresentation.INSTANCE));
			}
			else {
				List<Renderer> renderers = new ArrayList<>(pattern.length());
				int from = 0;
				for (int to = pattern.indexOf(DELIMITER); to != -1; to = pattern.indexOf(DELIMITER, from)) {
					if (to != 0 && pattern.charAt(to - 1) == ESCAPE && (to == 1 || pattern.charAt(to - 2) != ESCAPE)) {
						renderers.add(new Renderer.ForConstantValue(pattern.substring(from, Math.max(0, to - 1)) + DELIMITER));
						from = to + 1;
						continue;
					}
					else if (pattern.length() == to + 1) {
						throw new IllegalStateException("Missing sort descriptor for " + pattern + " at index " + to);
					}
					renderers.add(new Renderer.ForConstantValue(pattern.substring(from, to).replace("" + ESCAPE + ESCAPE, "" + ESCAPE)));
					switch (pattern.charAt(to + 1)) {
						case Renderer.ForMethodName.SYMBOL -> renderers.add(Renderer.ForMethodName.INSTANCE);
						case Renderer.ForTypeName.SYMBOL -> renderers.add(Renderer.ForTypeName.INSTANCE);
						case Renderer.ForDescriptor.SYMBOL -> renderers.add(Renderer.ForDescriptor.INSTANCE);
						case Renderer.ForReturnTypeName.SYMBOL -> renderers.add(Renderer.ForReturnTypeName.INSTANCE);
						case Renderer.ForJavaSignature.SYMBOL -> renderers.add(Renderer.ForJavaSignature.INSTANCE);
						case Renderer.ForPropertyName.SYMBOL -> renderers.add(Renderer.ForPropertyName.INSTANCE);
						default ->
								throw new IllegalStateException("Illegal sort descriptor " + pattern.charAt(to + 1) + " for " + pattern);
					}
					from = to + 2;
				}
				renderers.add(new Renderer.ForConstantValue(pattern.substring(from)));
				return new ForOrigin(renderers);
			}
		}

		@Override
		public Target resolve(TypeDescription type, MethodDescription method, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			StringBuilder builder = new StringBuilder();
			for (Renderer renderer : renderers) {
				builder.append(renderer.apply(type, method));
			}
			return Target.ForStackManipulation.of(builder.toString());
		}

		public interface Renderer {
			String apply(TypeDescription type, MethodDescription method);

			enum ForMethodName implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 'm';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return method.getInternalName();
				}
			}

			enum ForTypeName implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 't';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return type.getName();
				}
			}

			enum ForDescriptor implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 'd';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return method.getDescriptor();
				}
			}

			enum ForJavaSignature implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 's';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					StringBuilder builder = new StringBuilder('(');
					boolean comma = false;
					for (TypeDescription t : method.getParameters().asTypeList().asErasures()) {
						if (comma) {
							builder.append(',');
						}
						else {
							comma = true;
						}
						builder.append(type.getName());
					}
					return builder.append(')').toString();
				}
			}

			enum ForReturnTypeName implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 'r';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return method.getReturnType().asErasure().getName();
				}
			}

			enum ForStringRepresentation implements Renderer {
				INSTANCE;

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return method.toString();
				}
			}

			@AllArgsConstructor
			@HashCodeAndEqualsPlugin.Enhance
			class ForConstantValue implements Renderer {
				private final String value;

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return value;
				}
			}

			enum ForPropertyName implements Renderer {
				INSTANCE;

				public static final char SYMBOL = 'p';

				@Override
				public String apply(TypeDescription type, MethodDescription method) {
					return FieldAccessor.FieldNameExtractor.ForBeanProperty.INSTANCE.resolve(method);
				}
			}
		}

		public enum Factory implements OffsetMapping.Factory<Advice.Origin> {
			INSTANCE;

			private static final MethodDescription.InDefinedShape ORIGIN_VALUE =
					TypeDescription.ForLoadedType.of(Advice.Origin.class).getDeclaredMethods()
							.filter(named("value"))
							.getOnly();

			@Override
			public Class<Advice.Origin> getAnnotationType() {
				return Advice.Origin.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Origin> annotation, AdviceType adviceType) {
				TypeDescription type = target.getType().asErasure();
				if (type.represents(Class.class)) {
					return ForInstrumentedType.INSTANCE;
				}
				else if (type.represents(Method.class)) {
					return ForInstrumentedMethod.METHOD;
				}
				else if (type.represents(Constructor.class)) {
					return ForInstrumentedMethod.CONSTRUCTOR;
				}
				else if (JavaType.EXECUTABLE.getTypeStub().equals(type)) {
					return ForInstrumentedMethod.EXECUTABLE;
				}
				else if (type.isAssignableFrom(String.class)) {
					return ForOrigin.parse(annotation.getValue(ORIGIN_VALUE).resolve(String.class));
				}
				else {
					throw new IllegalStateException("Non-supported type " + target.getType() + " for @Origin annotation");
				}
			}
		}
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	class ForUnusedValue implements OffsetMapping {

		private final TypeDefinition target;

		@Override
		public Target resolve(TypeDescription instrumentedType, MethodDescription instrumentedMethod, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			return new Target.ForDefaultValue.ReadWrite(target);
		}

		public enum Factory implements OffsetMapping.Factory<Advice.Unused> {
			INSTANCE;

			@Override
			public Class<Advice.Unused> getAnnotationType() {
				return Advice.Unused.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Unused> annotation, AdviceType adviceType) {
				return new ForUnusedValue(target.getType());
			}
		}
	}

	enum ForStubValue implements OffsetMapping, Factory<Advice.StubValue> {
		INSTANCE;

		@Override
		public Target resolve(TypeDescription type, MethodDescription method, Assigner assigner, ArgumentHandler argumentHandler, Sort sort) {
			return new Target.ForDefaultValue.ReadOnly(method.getReturnType(),
					assigner.assign(method.getReturnType(), TypeDescription.Generic.OBJECT, Assigner.Typing.DYNAMIC));
		}

		@Override
		public Class<Advice.StubValue> getAnnotationType() {
			return Advice.StubValue.class;
		}

		@Override
		public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.StubValue> annotation, AdviceType adviceType) {
			if (!target.getType().represents(Object.class)) {
				throw new IllegalStateException("Cannot use StubValue on non-Object parameter type " + target);
			}
			return this;
		}
	}

	@AllArgsConstructor
	@HashCodeAndEqualsPlugin.Enhance
	class ForEnterValue implements OffsetMapping {
		private final TypeDescription.Generic target;
		private final TypeDescription.Generic enterType;
		private final boolean readOnly;
		private final Assigner.Typing typing;

		public ForEnterValue(TypeDescription.Generic target, TypeDescription.Generic enterType, AnnotationDescription.Loadable<Advice.Enter> annotation) {
			this(target, enterType,
					annotation.getValue(Factory.ENTER_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.ENTER_TYPING).load(Advice.Enter.class.getClassLoader()).resolve(Assigner.Typing.class));
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
		}

		@AllArgsConstructor
		@HashCodeAndEqualsPlugin.Enhance
		public static class Factory implements OffsetMapping.Factory<Advice.Enter> {
			private static final MethodDescription.InDefinedShape ENTER_READ_ONLY;
			private static final MethodDescription.InDefinedShape ENTER_TYPING;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.Enter.class).getDeclaredMethods();
				ENTER_READ_ONLY = methods.filter(named("readOnly")).getOnly();
				ENTER_TYPING = methods.filter(named("typing")).getOnly();
			}

			private final TypeDefinition enterType;

			public static OffsetMapping.Factory<Advice.Enter> of(TypeDefinition typeDefinition) {
				return typeDefinition.represents(void.class)
						? new Illegal<>(Advice.Enter.class)
						: new Factory(typeDefinition);
			}

			@Override
			public Class<Advice.Enter> getAnnotationType() {
				return Advice.Enter.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Enter> annotation, AdviceType adviceType) {
				if (adviceType.isDelegation() && !annotation.getValue(ENTER_READ_ONLY).resolve(Boolean.class)) {
					throw new IllegalStateException("Cannot use writable " + target + " on read-only parameter");
				}
				else {
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

		public ForExitValue(TypeDescription.Generic target, TypeDescription.Generic exitType, AnnotationDescription.Loadable<Advice.Exit> annotation) {
			this(target, exitType,
					annotation.getValue(Factory.EXIT_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.EXIT_TYPING).load(Advice.Exit.class.getClassLoader()).resolve(Assigner.Typing.class));
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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		public static class Factory implements OffsetMapping.Factory<Advice.Exit> {
			private static final MethodDescription.InDefinedShape EXIT_READ_ONLY;
			private static final MethodDescription.InDefinedShape EXIT_TYPING;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.Exit.class).getDeclaredMethods();
				EXIT_READ_ONLY = methods.filter(named("typing")).getOnly();
				EXIT_TYPING = methods.filter(named("typing")).getOnly();
			}

			private final TypeDefinition exitType;

			public static OffsetMapping.Factory<Advice.Exit> of(TypeDefinition typeDefinition) {
				return typeDefinition.represents(void.class)
						? new Illegal<>(Advice.Exit.class)
						: new Factory(typeDefinition);
			}

			@Override
			public Class<Advice.Exit> getAnnotationType() {
				return Advice.Exit.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Exit> annotation, AdviceType adviceType) {
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
	public class ForLocalValue implements OffsetMapping {
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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		public static class Factory implements OffsetMapping.Factory<Advice.Local> {
			public static final MethodDescription.InDefinedShape LOCAL_VALUE = TypeDescription.ForLoadedType.of(Advice.Local.class)
					.getDeclaredMethods()
					.filter(named("value"))
					.getOnly();
			private final Map<String, TypeDefinition> namedTypes;

			@Override
			public Class<Advice.Local> getAnnotationType() {
				return Advice.Local.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Local> annotation, AdviceType adviceType) {
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

		public ForReturnValue(TypeDescription.Generic target, AnnotationDescription.Loadable<Advice.Return> annotation) {
			this(target,
					annotation.getValue(Factory.RETURN_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.RETURN_TYPING).load(Advice.Return.class.getClassLoader()).resolve(Assigner.Typing.class));
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

		public enum Factory implements OffsetMapping.Factory<Advice.Return> {
			INSTANCE;

			private static final MethodDescription.InDefinedShape RETURN_READ_ONLY;
			private static final MethodDescription.InDefinedShape RETURN_TYPING;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.Return.class).getDeclaredMethods();
				RETURN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
				RETURN_TYPING = methods.filter(named("typing")).getOnly();
			}

			@Override
			public Class<Advice.Return> getAnnotationType() {
				return Advice.Return.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Return> annotation, AdviceType adviceType) {
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

		public ForThrowable(TypeDescription.Generic target, AnnotationDescription.Loadable<Advice.Thrown> annotation) {
			this(target,
					annotation.getValue(Factory.THROWN_READ_ONLY).resolve(Boolean.class),
					annotation.getValue(Factory.THROWN_TYPING).load(Advice.Thrown.class.getClassLoader()).resolve(Assigner.Typing.class));
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

		public enum Factory implements OffsetMapping.Factory<Advice.Thrown> {
			INSTANCE;

			private static final MethodDescription.InDefinedShape THROWN_READ_ONLY;

			private static final MethodDescription.InDefinedShape THROWN_TYPING;

			static {
				MethodList<MethodDescription.InDefinedShape> methods = TypeDescription.ForLoadedType.of(Advice.Thrown.class).getDeclaredMethods();
				THROWN_READ_ONLY = methods.filter(named("readOnly")).getOnly();
				THROWN_TYPING = methods.filter(named("typing")).getOnly();
			}

			public static OffsetMapping.Factory<?> of(MethodDescription.InDefinedShape adviceMethod) {
				return isNoExceptionHandler(adviceMethod.getDeclaredAnnotations()
						.ofType(Advice.OnMethodExit.class)
						.getValue(ON_THROWABLE)
						.resolve(TypeDescription.class))
						? new OffsetMapping.Factory.Illegal<>(Advice.Thrown.class) : Factory.INSTANCE;
			}

			@Override
			public Class<Advice.Thrown> getAnnotationType() {
				return Advice.Thrown.class;
			}

			@Override
			public OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<Advice.Thrown> annotation, AdviceType adviceType) {
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

		@AllArgsConstructor(access = AccessLevel.public)
		@HashCodeAndEqualsPlugin.Enhance
		public static class OfAnnotationProperty<T extends Annotation> implements OffsetMapping.Factory<T> {
			private final Class<T> annotationType;
			private final MethodDescription.InDefinedShape property;

			public static <S extends Annotation> OffsetMapping.Factory<S> of(Class<S> annotationType, String property) {
				if (!annotationType.isAnnotation()) {
					throw new IllegalArgumentException("Not an annotation type: " + annotationType);
				}
				try {
					return new OfAnnotationProperty<>(annotationType, new MethodDescription.ForLoadedMethod(annotationType.getMethod(property)));
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

				MethodList<MethodDescription.InGenericShape> methodCandidates = target.getType().getDeclaredMethods().filter(isAbstract());
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

		@AllArgsConstructor(access = AccessLevel.public)
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
