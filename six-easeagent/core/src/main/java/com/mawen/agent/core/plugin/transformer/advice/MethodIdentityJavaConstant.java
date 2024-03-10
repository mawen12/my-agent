package com.mawen.agent.core.plugin.transformer.advice;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.utility.JavaConstant;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public record MethodIdentityJavaConstant(Integer identify) implements JavaConstant {

	@Override
	public Object toDescription() {
		return this.identify;
	}

	@Override
	public TypeDescription getTypeDescription() {
		return TypeDescription.ForLoadedType.of(int.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T accept(Visitor<T> visitor) {
		return (T) this.identify;
	}
}
