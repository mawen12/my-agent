package com.mawen.agent.plugin.processor;

import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.squareup.javapoet.ClassName;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
abstract class BeanUtils {

	private final Elements elements;
	private final Types types;

	private BeanUtils(ProcessingEnvironment pe) {
		elements = pe.getElementUtils();
		types = pe.getTypeUtils();
	}

	static BeanUtils of(final ProcessingEnvironment pe) {
		return new BeanUtils(pe) {};
	}

	Element asElement(TypeMirror t) {
		return types.asElement(t);
	}

	ClassName classNameOf(TypeElement e) {
		return ClassName.get(e);
	}

	String packageNameOf(TypeElement e) {
		return ClassName.get(e).packageName();
	}

	TypeElement asTypeElement(Supplier<Class<?>> supplier) {
		try {
			Class<?> clazz = supplier.get();
			return getTypeElement(clazz.getCanonicalName());
		}
		catch (MirroredTypeException e) {
			return (TypeElement) asElement(e.getTypeMirror());
		}
	}

	boolean isSameType(TypeMirror t, String canonical) {
		return isSameType(t, getTypeElement(canonical).asType());
	}

	boolean isSameType(TypeMirror t1, TypeMirror t2) {
		return types.isSameType(t1, t2);
	}

	boolean isAssignable(TypeElement t1, TypeElement t2) {
		TypeMirror tm1 = t1.asType();
		TypeMirror tm2 = t2.asType();
		return types.isAssignable(tm1, tm2);
	}

	public TypeElement getTypeElement(CharSequence name) {
		return elements.getTypeElement(name);
	}
}
