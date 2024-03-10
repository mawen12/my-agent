package com.mawen.agent.plugin.matcher;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.mawen.agent.plugin.asm.Modifier;
import com.mawen.agent.plugin.enums.Operator;
import com.mawen.agent.plugin.enums.StringMatch;
import com.mawen.agent.plugin.matcher.operator.AndMethodMatcher;
import com.mawen.agent.plugin.matcher.operator.OrMethodMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class MethodMatcher implements IMethodMatcher {
	private String name; // method name
	private StringMatch nameMatchType; // the match type of method name
	private String returnType = null; // ignored when with default value
	private String[] args; // types of method arguments
	private int argsLength = -1;
	private int modifier = Modifier.ACC_NONE;
	private int notModifier = Modifier.ACC_NONE;

	protected IClassMatcher overriddenFrom = null;

	private String qualifier;

	public static final int MODIFIER_MASK = Modifier.ACC_ABSTRACT | Modifier.ACC_STATIC
			| Modifier.ACC_PRIVATE | Modifier.ACC_PUBLIC | Modifier.ACC_PROTECTED;

	protected MethodMatcher() {
	}

	public MethodMatcher(String name, StringMatch nameMatchType, String returnType,
			String[] args, int argsLength, int modifier,
			int notModifier, String qualifier, IClassMatcher overriddenFrom) {
		this.name = name;
		this.nameMatchType = nameMatchType;
		this.returnType = returnType;
		this.args = args;
		this.argsLength = argsLength;
		this.modifier = modifier;
		this.notModifier = notModifier;
		this.qualifier = qualifier;
		this.overriddenFrom = overriddenFrom;
	}

	@Override
	public boolean isDefaultQualifier() {
		return this.qualifier.equals(DEFAULT_QUALIFIER);
	}

	@Override
	public String getQualifier() {
		return qualifier;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public StringMatch getNameMatchType() {
		return nameMatchType;
	}

	public void setNameMatchType(StringMatch nameMatchType) {
		this.nameMatchType = nameMatchType;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public int getArgsLength() {
		return argsLength;
	}

	public void setArgsLength(int argsLength) {
		this.argsLength = argsLength;
	}

	public int getModifier() {
		return modifier;
	}

	public void setModifier(int modifier) {
		this.modifier = modifier;
	}

	public int getNotModifier() {
		return notModifier;
	}

	public void setNotModifier(int notModifier) {
		this.notModifier = notModifier;
	}

	public IClassMatcher getOverriddenFrom() {
		return overriddenFrom;
	}

	public void setOverriddenFrom(IClassMatcher overriddenFrom) {
		this.overriddenFrom = overriddenFrom;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		MethodMatcher matcher = (MethodMatcher) o;
		return argsLength == matcher.argsLength && modifier == matcher.modifier && notModifier == matcher.notModifier && Objects.equals(name, matcher.name) && nameMatchType == matcher.nameMatchType && Objects.equals(returnType, matcher.returnType) && Arrays.equals(args, matcher.args) && Objects.equals(overriddenFrom, matcher.overriddenFrom) && Objects.equals(qualifier, matcher.qualifier);
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(name, nameMatchType, returnType, argsLength, modifier, notModifier, overriddenFrom, qualifier);
		result = 31 * result + Arrays.hashCode(args);
		return result;
	}

	@Override
	public String toString() {
		return "MethodMatcher{" +
				"name='" + name + '\'' +
				", nameMatchType=" + nameMatchType +
				", returnType='" + returnType + '\'' +
				", args=" + Arrays.toString(args) +
				", argsLength=" + argsLength +
				", modifier=" + modifier +
				", notModifier=" + notModifier +
				", overriddenFrom=" + overriddenFrom +
				", qualifier='" + qualifier + '\'' +
				'}';
	}

	public static MethodMatcherBuilder builder() {
		return new MethodMatcherBuilder();
	}

	public static class MethodMatcherBuilder {
		private String name;
		private StringMatch nameMatchType;
		private String returnType;
		private String[] args;
		private int argsLength = -1;
		private int modifier;
		private int notModifier;

		protected IClassMatcher isOverriddenFrom;
		private String qualifier = IMethodMatcher.DEFAULT_QUALIFIER;
		private Operator operator = Operator.AND;
		private boolean isNegate = false;
		private IMethodMatcher left;

		MethodMatcherBuilder() {
		}

		public MethodMatcherBuilder or() {
			return operate(Operator.OR);
		}

		public MethodMatcherBuilder and() {
			return operate(Operator.AND);
		}

		public MethodMatcherBuilder negate() {
			this.isNegate = true;
			return this;
		}

		public MethodMatcherBuilder named(String methodName) {
			if (methodName.equals("<init>")) {
				return isConstruct();
			}
			return this.name(methodName).nameMatchType(StringMatch.EQUALS);
		}

		public MethodMatcherBuilder isConstruct() {
			return this.name("<init>").nameMatchType(StringMatch.EQUALS);
		}

		public MethodMatcherBuilder nameStartWith(String methodName) {
			return this.name(methodName).nameMatchType(StringMatch.START_WITH);
		}

		public MethodMatcherBuilder nameEndWith(String methodName) {
			return this.name(methodName).nameMatchType(StringMatch.END_WITH);
		}

		public MethodMatcherBuilder nameContains(String methodName) {
			return this.name(methodName).nameMatchType(StringMatch.CONTAINS);
		}

		public MethodMatcherBuilder isPublic() {
			this.modifier |= Modifier.ACC_PUBLIC;
			return this;
		}

		public MethodMatcherBuilder isPrivate() {
			this.modifier |= Modifier.ACC_PRIVATE;
			return this;
		}

		public MethodMatcherBuilder isAbstract() {
			this.modifier |= Modifier.ACC_ABSTRACT;
			return this;
		}

		public MethodMatcherBuilder isStatic() {
			this.modifier |= Modifier.ACC_STATIC;
			return this;
		}

		public MethodMatcherBuilder notPublic() {
			this.notModifier |= Modifier.ACC_PUBLIC;
			return this;
		}

		public MethodMatcherBuilder notPrivate() {
			this.notModifier |= Modifier.ACC_PRIVATE;
			return this;
		}

		public MethodMatcherBuilder notAbstract() {
			this.notModifier |= Modifier.ACC_ABSTRACT;
			return this;
		}

		public MethodMatcherBuilder notStatic() {
			this.notModifier |= Modifier.ACC_STATIC;
			return this;
		}

		protected MethodMatcherBuilder name(String name) {
			this.name = name;
			return this;
		}

		public MethodMatcherBuilder nameMatchType(StringMatch nameMatchType) {
			this.nameMatchType = nameMatchType;
			return this;
		}

		public MethodMatcherBuilder returnType(String returnType) {
			this.returnType = returnType;
			return this;
		}

		public MethodMatcherBuilder args(String[] args) {
			this.args = args;
			return this;
		}

		public MethodMatcherBuilder arg(int idx, String argType) {
			if (args == null || args.length < 4) {
				this.args = new String[idx > 4 ? idx + 1 : 5];
			}
			else if (this.args.length < idx + 1) {
				this.args = Arrays.copyOf(this.args, idx + 1);
			}
			this.args[idx] = argType;

			return this;
		}

		public MethodMatcherBuilder argsLength(int length) {
			this.argsLength = length;

			if (length <= 0) {
				this.args = null;
			}
			else if (this.args == null) {
				this.args = new String[length];
			}
			else if (this.args.length < length) {
				this.args = Arrays.copyOf(this.args, length);
			}

			return this;
		}

		public MethodMatcherBuilder modifier(int modifier) {
			this.modifier = modifier;
			return this;
		}

		public MethodMatcherBuilder qualifier(String qualifier) {
			// each builder can only assigned a qualifier
			if (!this.qualifier.equals(IMethodMatcher.DEFAULT_QUALIFIER)) {
				throw new RuntimeException("Qualifier has already been assigned");
			}
			this.qualifier = qualifier;
			return this;
		}

		public MethodMatcherBuilder isOverriddenFrom(IClassMatcher cMatcher) {
			this.isOverriddenFrom = cMatcher;
			return this;
		}

		public IMethodMatcher build() {
			IMethodMatcher matcher = new MethodMatcher(name, nameMatchType, returnType, args, argsLength, modifier, notModifier, qualifier, isOverriddenFrom);

			if (this.isNegate) {
				matcher = matcher.negate();
			}

			if (this.left == null || this.operator == null) {
				return matcher;
			}
			return switch (this.operator) {
				case OR -> new OrMethodMatcher(this.left, matcher);
				case AND -> new AndMethodMatcher(this.left, matcher);
				default -> matcher;
			};
		}

		private MethodMatcherBuilder operate(Operator opt) {
			var builder = new MethodMatcherBuilder();
			builder.left = this.build();
			builder.operator = opt;

			if (!builder.left.isDefaultQualifier()) {
				builder.qualifier(builder.left.getQualifier());
			}
			return builder;
		}

		@Override
		public String toString() {
			return "MethodMatcherBuilder{" +
					"name='" + name + '\'' +
					", nameMatchType=" + nameMatchType +
					", returnType='" + returnType + '\'' +
					", args=" + Arrays.toString(args) +
					", argsLength=" + argsLength +
					", modifier=" + modifier +
					", notModifier=" + notModifier +
					", isOverriddenFrom=" + isOverriddenFrom +
					", qualifier='" + qualifier + '\'' +
					", operator=" + operator +
					", isNegate=" + isNegate +
					", left=" + left +
					'}';
		}
	}

	public static MethodMatchersBuilder multiBuilder() {
		return new MethodMatchersBuilder();
	}

	public static class MethodMatchersBuilder {
		private Set<IMethodMatcher> methodMatchers;

		MethodMatchersBuilder() {
		}

		public MethodMatchersBuilder methodMatchers(Set<IMethodMatcher> methodMatchers) {
			this.methodMatchers = methodMatchers;
			return this;
		}

		public MethodMatchersBuilder matcher(IMethodMatcher matcher) {
			if (matcher == null) {
				return this;
			}
			if (this.methodMatchers == null) {
				this.methodMatchers = new LinkedHashSet<>();
			}
			this.methodMatchers.add(matcher);
			return this;
		}

		public Set<IMethodMatcher> build() {
			return this.methodMatchers;
		}

		@Override
		public String toString() {
			return "MethodMatchersBuilder{" +
					"methodMatchers=" + methodMatchers +
					'}';
		}
	}
}
