package com.mawen.agent.plugin.processor;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class RepeatedAnnotationVisitor implements AnnotationValueVisitor<Set<AnnotationMirror>, Class<? extends Annotation>> {

	@Override
	public Set<AnnotationMirror> visit(AnnotationValue av, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitBoolean(boolean b, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitByte(byte b, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitChar(char c, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitDouble(double d, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitFloat(float f, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitInt(int i, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitLong(long i, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitShort(short s, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitString(String s, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitType(TypeMirror t, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitEnumConstant(VariableElement c, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}

	@Override
	public Set<AnnotationMirror> visitAnnotation(AnnotationMirror a, Class<? extends Annotation> aClass) {
		return Collections.singleton(a);
	}

	@Override
	public Set<AnnotationMirror> visitArray(List<? extends AnnotationValue> vals, Class<? extends Annotation> aClass) {
		final Set<AnnotationMirror> accept = new HashSet<>();

		for (AnnotationValue v : vals) {
			accept.addAll(v.accept(this, aClass));
		}

		return accept;
	}

	@Override
	public Set<AnnotationMirror> visitUnknown(AnnotationValue av, Class<? extends Annotation> aClass) {
		return Collections.emptySet();
	}
}
