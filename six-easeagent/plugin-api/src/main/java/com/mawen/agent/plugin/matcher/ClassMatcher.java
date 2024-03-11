package com.mawen.agent.plugin.matcher;

import com.mawen.agent.plugin.asm.Modifier;
import com.mawen.agent.plugin.enums.ClassMatch;
import com.mawen.agent.plugin.enums.Operator;
import com.mawen.agent.plugin.matcher.operator.AndClassMatcher;
import com.mawen.agent.plugin.matcher.operator.OrClassMatcher;
import lombok.Data;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/1
 */
@Data
public class ClassMatcher implements IClassMatcher {

	public static final int MODIFIER_MASK = Modifier.ACC_ABSTRACT | Modifier.ACC_INTERFACE
			| Modifier.ACC_PRIVATE | Modifier.ACC_PUBLIC | Modifier.ACC_PROTECTED;

	private String name;
	private ClassMatch matchType;
	private int modifier = Modifier.ACC_NONE;
	private int notModifier = Modifier.ACC_NONE;


	protected ClassMatcher(){}

	private ClassMatcher(String name, ClassMatch type, int modifier, int notModifier) {
		this.name = name;
		this.matchType = type;
		this.modifier = modifier;
		this.notModifier = notModifier;
	}

	public static ClassMatcherBuilder builder() {
		return new ClassMatcherBuilder();
	}

	public static class ClassMatcherBuilder {

		private String name;
		private ClassMatch matchType;
		private int modifier;
		private int notModifier;

		private IClassMatcher left;
		private Operator operator = Operator.AND;
		private boolean isNegate = false;

		ClassMatcherBuilder(){}

		public ClassMatcherBuilder or() {
			return operator(Operator.OR);
		}

		public ClassMatcherBuilder and() {
			return operator(Operator.AND);
		}

		private ClassMatcherBuilder operator(Operator operator) {
			ClassMatcherBuilder builder = new ClassMatcherBuilder();
			builder.left = this.build();
			builder.operator = operator;

			return builder;
		}

		public ClassMatcherBuilder negate() {
			this.isNegate = true;
			return this;
		}

		public ClassMatcherBuilder hasSuperClass(String className) {
			if (this.name != null && !this.name.isEmpty()) {
				if (this.matchType.equals(ClassMatch.SUPER_CLASS)) {
					// replace
					return this.name(className).matchType(ClassMatch.SUPER_CLASS);
				} else {
					// and operate
					ClassMatcherBuilder builder = new ClassMatcherBuilder();
					builder.hasSuperClass(className).matchType(ClassMatch.SUPER_CLASS);
					builder.left = this.build();
					builder.operator = Operator.AND;
					return builder;
				}
			}
			return this.name(className).matchType(ClassMatch.SUPER_CLASS);
		}

		public ClassMatcherBuilder hasClassName(String className) {
			return this.name(className).matchType(ClassMatch.NAMED);
		}

		public ClassMatcherBuilder hasAnnotation(String className) {
			return this.name(className).matchType(ClassMatch.ANNOTATION);
		}

		public ClassMatcherBuilder hasInterface(String className) {
			if (this.name != null && !this.name.isEmpty()) {
				// and operate
				ClassMatcherBuilder builder = new ClassMatcherBuilder();
				builder.hasSuperClass(className).matchType(ClassMatch.INTERFACE);
				builder.left = this.build();
				builder.operator = Operator.AND;
				return builder;
			}
			return this.name(className).matchType(ClassMatch.INTERFACE);
		}

		public ClassMatcherBuilder matchType(ClassMatch matchType) {
			this.matchType = matchType;
			return this;
		}

		public ClassMatcherBuilder modifier(int modifier) {
			this.modifier = modifier;
			return this;
		}

		public ClassMatcherBuilder notModifier(int notModifier) {
			this.notModifier = notModifier;
			return this;
		}

		public ClassMatcherBuilder isPublic() {
			this.modifier |= Modifier.ACC_PUBLIC;
			return this;
		}

		public ClassMatcherBuilder isPrivate() {
			this.modifier |= Modifier.ACC_PRIVATE;
			return this;
		}

		public ClassMatcherBuilder isAbstract() {
			this.modifier |= Modifier.ACC_ABSTRACT;
			return this;
		}

		public ClassMatcherBuilder isInterface() {
			this.modifier |= Modifier.ACC_INTERFACE;
			return this;
		}

		public ClassMatcherBuilder notPrivate() {
			this.notModifier |= Modifier.ACC_PRIVATE;
			return this;
		}

		public ClassMatcherBuilder notAbstract() {
			this.notModifier |= Modifier.ACC_ABSTRACT;
			return this;
		}

		public ClassMatcherBuilder notInterface() {
			this.notModifier |= Modifier.ACC_INTERFACE;
			return this;
		}

		public ClassMatcherBuilder name(String name) {
			this.name = name;
			return this;
		}

		public IClassMatcher build() {
			IClassMatcher matcher = new ClassMatcher(this.name, this.matchType, this.modifier, this.notModifier);

			if (this.isNegate) {
				matcher = matcher.negate();
			}

			if (this.left == null || this.operator == null) {
				return matcher;
			}

			matcher = switch (this.operator) {
				case OR -> new OrClassMatcher(this.left, matcher);
				case AND -> new AndClassMatcher(this.left, matcher);
				default -> matcher;
			};
			return matcher;
		}
	}

}
