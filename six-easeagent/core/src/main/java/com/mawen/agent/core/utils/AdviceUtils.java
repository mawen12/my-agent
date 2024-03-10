package com.mawen.agent.core.utils;

import net.bytebuddy.description.enumeration.EnumerationDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.StackManipulation;
import net.bytebuddy.implementation.bytecode.constant.ClassConstant;
import net.bytebuddy.implementation.bytecode.constant.DoubleConstant;
import net.bytebuddy.implementation.bytecode.constant.FloatConstant;
import net.bytebuddy.implementation.bytecode.constant.IntegerConstant;
import net.bytebuddy.implementation.bytecode.constant.JavaConstantValue;
import net.bytebuddy.implementation.bytecode.constant.LongConstant;
import net.bytebuddy.implementation.bytecode.constant.TextConstant;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.jar.asm.Opcodes;
import net.bytebuddy.utility.JavaConstant;
import net.bytebuddy.utility.JavaType;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/10
 */
public class AdviceUtils {

	public static StackManipulation getStackManipulation(Object value) {
		if (value instanceof Boolean b) {
			return IntegerConstant.forValue(b);
		}
		else if (value instanceof Byte b) {
			return IntegerConstant.forValue(b);
		}
		else if (value instanceof Short s) {
			return IntegerConstant.forValue(s);
		}
		else if (value instanceof Character c) {
			return IntegerConstant.forValue(c);
		}
		else if (value instanceof Integer i) {
			return IntegerConstant.forValue(i);
		}
		else if (value instanceof Long l) {
			return LongConstant.forValue(l);
		}
		else if (value instanceof Float f) {
			return FloatConstant.forValue(f);
		}
		else if (value instanceof Double d) {
			return DoubleConstant.forValue(d);
		}
		else if (value instanceof String s) {
			return new TextConstant(s);
		}
		else if (value instanceof Class<?> c) {
			return ClassConstant.of(TypeDescription.ForLoadedType.of(c));
		}
		else if (value instanceof TypeDescription t) {
			return ClassConstant.of(t);
		}
		else if (value instanceof Enum<?> e) {
			return FieldAccess.forEnumeration(new EnumerationDescription.ForLoadedEnumeration(e));
		}
		else if (value instanceof EnumerationDescription e) {
			return FieldAccess.forEnumeration(e);
		}
		else if (JavaType.METHOD_HANDLE.isInstance(value)) {
			return new JavaConstantValue(JavaConstant.MethodHandle.ofLoaded(value));
		}
		else if (JavaType.METHOD_TYPE.isInstance(value)) {
			return new JavaConstantValue(JavaConstant.MethodType.ofLoaded(value));
		}
		else if (value instanceof JavaConstant j) {
			return new JavaConstantValue(j);
		}
		return null;
	}

	public static TypeDescription getTypeDescription(Object value) {
		if (value instanceof Boolean b) {
			return TypeDescription.ForLoadedType.of(boolean.class);
		}
		else if (value instanceof Byte b) {
			return TypeDescription.ForLoadedType.of(byte.class);
		}
		else if (value instanceof Short s) {
			return TypeDescription.ForLoadedType.of(short.class);
		}
		else if (value instanceof Character c) {
			return TypeDescription.ForLoadedType.of(char.class);
		}
		else if (value instanceof Integer i) {
			return TypeDescription.ForLoadedType.of(int.class);
		}
		else if (value instanceof Long l) {
			return TypeDescription.ForLoadedType.of(long.class);
		}
		else if (value instanceof Float f) {
			return TypeDescription.ForLoadedType.of(float.class);
		}
		else if (value instanceof Double d) {
			return TypeDescription.ForLoadedType.of(double.class);
		}
		else if (value instanceof String s) {
			return TypeDescription.ForLoadedType.of(String.class);
		}
		else if (value instanceof Class<?> c) {
			return TypeDescription.ForLoadedType.of(c);
		}
		else if (value instanceof TypeDescription t) {
			return TypeDescription.ForLoadedType.of(TypeDescription.class);
		}
		else if (value instanceof Enum<?> e) {
			return TypeDescription.ForLoadedType.of(e.getDeclaringClass());
		}
		else if (value instanceof EnumerationDescription e) {
			return e.getEnumerationType();
		}
		else if (JavaType.METHOD_HANDLE.isInstance(value)) {
			return JavaConstant.MethodHandle.ofLoaded(value).getTypeDescription();
		}
		else if (JavaType.METHOD_TYPE.isInstance(value)) {
			return JavaConstant.MethodType.ofLoaded(value).getTypeDescription();
		}
		else if (value instanceof JavaConstant j) {
			return j.getTypeDescription();
		}
		return null;
	}

	public static int getPushConstant(TypeDescription.Generic returnType) {
		if (returnType.represents(boolean.class)) {
			return Opcodes.ICONST_0;
		}
		else if (returnType.represents(byte.class)) {
			return Opcodes.ICONST_0;
		}
		else if (returnType.represents(short.class)) {
			return Opcodes.ICONST_0;
		}
		else if (returnType.represents(char.class)) {
			return Opcodes.ICONST_0;
		}
		else if (returnType.represents(int.class)) {
			return Opcodes.ICONST_0;
		}
		else if (returnType.represents(long.class)) {
			return Opcodes.LCONST_0;
		}
		else if (returnType.represents(float.class)) {
			return Opcodes.FCONST_0;
		}
		else if (returnType.represents(double.class)) {
			return Opcodes.DCONST_0;
		}
		else if (returnType.represents(void.class)) {
			return Opcodes.RETURN;
		}
		else {
			return Opcodes.ACONST_NULL;
		}
	}

	public static int getStoreConstant(TypeDescription.Generic returnType) {
		if (returnType.represents(boolean.class)) {
			return Opcodes.ISTORE;
		}
		else if (returnType.represents(byte.class)) {
			return Opcodes.ISTORE;
		}
		else if (returnType.represents(short.class)) {
			return Opcodes.ISTORE;
		}
		else if (returnType.represents(char.class)) {
			return Opcodes.ISTORE;
		}
		else if (returnType.represents(int.class)) {
			return Opcodes.ISTORE;
		}
		else if (returnType.represents(long.class)) {
			return Opcodes.LSTORE;
		}
		else if (returnType.represents(float.class)) {
			return Opcodes.FSTORE;
		}
		else if (returnType.represents(double.class)) {
			return Opcodes.DSTORE;
		}
		else if (returnType.represents(void.class)) {
			return Opcodes.ASTORE;
		}
		return Opcodes.NULL;
	}

	public static int getReturnConstant(TypeDescription.Generic returnType) {
		if (returnType.represents(boolean.class)) {
			return Opcodes.IRETURN;
		}
		else if (returnType.represents(byte.class)) {
			return Opcodes.IRETURN;
		}
		else if (returnType.represents(short.class)) {
			return Opcodes.IRETURN;
		}
		else if (returnType.represents(char.class)) {
			return Opcodes.IRETURN;
		}
		else if (returnType.represents(int.class)) {
			return Opcodes.IRETURN;
		}
		else if (returnType.represents(long.class)) {
			return Opcodes.LRETURN;
		}
		else if (returnType.represents(float.class)) {
			return Opcodes.FRETURN;
		}
		else if (returnType.represents(double.class)) {
			return Opcodes.DRETURN;
		}
		else if (returnType.represents(void.class)) {
			return Opcodes.RETURN;
		}
		else {
			return Opcodes.ARETURN;
		}
	}

	public static Object getFrameElementType(TypeDescription typeDescription) {
		if (typeDescription.represents(boolean.class)) {
			return Opcodes.INTEGER;
		}
		else if (typeDescription.represents(byte.class)) {
			return Opcodes.INTEGER;
		}
		else if (typeDescription.represents(short.class)) {
			return Opcodes.INTEGER;
		}
		else if (typeDescription.represents(char.class)) {
			return Opcodes.INTEGER;
		}
		else if (typeDescription.represents(int.class)) {
			return Opcodes.INTEGER;
		}
		else if (typeDescription.represents(long.class)) {
			return Opcodes.LONG;
		}
		else if (typeDescription.represents(float.class)) {
			return Opcodes.FLOAT;
		}
		else if (typeDescription.represents(double.class)) {
			return Opcodes.DOUBLE;
		}
		else {
			return typeDescription.getInternalName();
		}
	}

	private AdviceUtils() {
	}
}
