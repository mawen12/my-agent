package com.mawen.agent.plugin.processor;

import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.SimpleElementVisitor8;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ElementVisitor8 extends SimpleElementVisitor8<TypeElement, TypeElement> {

	private final BeanUtils utils;

	public ElementVisitor8(BeanUtils utils) {
		this.utils = utils;
	}

	@Override
	public TypeElement visitPackage(PackageElement packageElement, TypeElement p) {
		return null;
	}

	@Override
	public TypeElement visitType(TypeElement enclosingClass, TypeElement p) {
		if (utils.isAssignable(enclosingClass, p)) {
			return enclosingClass;
		}
		return null;
	}

	@Override
	public TypeElement visitUnknown(Element e, TypeElement typeElement) {
		return null;
	}

	@Override
	protected TypeElement defaultAction(Element e, TypeElement p) {
		throw new IllegalArgumentException("Unexpected type nesting: " + p);
	}
}
