package com.mawen.agent.core.plugin.matcher;

import java.util.Set;

import com.mawen.agent.plugin.Ordered;
import com.mawen.agent.plugin.utils.NoNull;
import lombok.Getter;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
@Getter
public class ClassTransformation implements Ordered {

	private int order;
	private ElementMatcher.Junction<TypeDescription> classMatcher;
	private ElementMatcher<ClassLoader> classLoaderMatcher;
	private Set<MethodTransformation> methodTransformations;
	private boolean hasDynamicField;

	public ClassTransformation(int order, ElementMatcher.Junction<TypeDescription> classMatcher, ElementMatcher<ClassLoader> classLoaderMatcher, Set<MethodTransformation> methodTransformations, boolean hasDynamicField) {
		this.order = order;
		this.classMatcher = classMatcher;
		this.classLoaderMatcher = NoNull.of(classLoaderMatcher, any());
		this.methodTransformations = methodTransformations;
		this.hasDynamicField = hasDynamicField;
	}

	@Override
	public int order() {
		return order;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int order;
		private ElementMatcher.Junction<TypeDescription> classMatcher;
		private ElementMatcher<ClassLoader> classLoaderMatcher;
		private Set<MethodTransformation> methodTransformations;
		private boolean hasDynamicField;

		Builder(){}

		public Builder order(int order) {
			this.order = order;
			return this;
		}

		public Builder classMatcher(ElementMatcher.Junction<TypeDescription> classMatcher) {
			this.classMatcher = classMatcher;
			return this;
		}

		public Builder classLoaderMatcher(ElementMatcher<ClassLoader> classLoaderMatcher) {
			this.classLoaderMatcher = classLoaderMatcher;
			return this;
		}

		public Builder methodTransformations(Set<MethodTransformation> methodTransformations) {
			this.methodTransformations = methodTransformations;
			return this;
		}

		public Builder hasDynamicField(boolean hasDynamicField) {
			this.hasDynamicField = hasDynamicField;
			return this;
		}

		public ClassTransformation build() {
			return new ClassTransformation(order, classMatcher, classLoaderMatcher, methodTransformations, hasDynamicField);
		}

		@Override
		public String toString() {
			return "ClassTransformation.Builder{" +
					"order=" + order +
					", classMatcher=" + classMatcher +
					", classLoaderMatcher=" + classLoaderMatcher +
					", methodTransformations=" + methodTransformations +
					", hasDynamicField=" + hasDynamicField +
					'}';
		}
	}
}
