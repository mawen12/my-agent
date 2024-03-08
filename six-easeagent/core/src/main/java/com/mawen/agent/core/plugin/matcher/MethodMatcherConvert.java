package com.mawen.agent.core.plugin.matcher;

import com.mawen.agent.plugin.asm.Modifier;
import com.mawen.agent.plugin.matcher.ClassMatcher;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.MethodMatcher;
import com.mawen.agent.plugin.matcher.operator.AndMethodMatcher;
import com.mawen.agent.plugin.matcher.operator.NegateMethodMatcher;
import com.mawen.agent.plugin.matcher.operator.OrMethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NegatingMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class MethodMatcherConvert implements Converter<IMethodMatcher, ElementMatcher.Junction<MethodDescription>> {

	public static final MethodMatcherConvert INSTANCE = new MethodMatcherConvert();

	@Override
	public ElementMatcher.Junction<MethodDescription> convert(IMethodMatcher source) {
		if (source == null) {
			return null;
		}

		if (source instanceof AndMethodMatcher andMatcher) {
			ElementMatcher.Junction<MethodDescription> leftMatcher = this.convert(andMatcher.getLeft());
			ElementMatcher.Junction<MethodDescription> rightMatcher = this.convert(andMatcher.getRight());
			return leftMatcher.and(rightMatcher);
		}
		else if (source instanceof OrMethodMatcher orMatcher) {
			ElementMatcher.Junction<MethodDescription> leftMatcher = this.convert(orMatcher.getLeft());
			ElementMatcher.Junction<MethodDescription> rightMatcher = this.convert(orMatcher.getRight());
			return leftMatcher.or(rightMatcher);
		}
		else if (source instanceof NegateMethodMatcher matcher) {
			ElementMatcher.Junction<MethodDescription> notMatcher = this.convert(matcher.getMatcher());
			return new NegatingMatcher<>(notMatcher);
		}

		if (!(source instanceof MethodMatcher)) {
			return null;
		}

		return this.convert((MethodMatcher) source);
	}

	private ElementMatcher.Junction<MethodDescription> convert(MethodMatcher matcher) {
		ElementMatcher.Junction<MethodDescription> c = null;
		if (matcher.getName() != null && matcher.getNameMatchType() != null) {
			switch (matcher.getNameMatchType()) {
				case EQUALS:
					if ("<init>".equals(matcher.getName())) {
						c = isConstructor();
					}
					else {
						c = named(matcher.getName());
					}
					break;
				case START_WITH:
					c = nameStartsWith(matcher.getName());
					break;
				case END_WITH:
					c = nameEndsWith(matcher.getName());
					break;
				case CONTAINS:
					c = nameContains(matcher.getName());
					break;
				default:
					return null;
			}
		}

		ElementMatcher.Junction<MethodDescription> mc = fromModifier(matcher.getModifier(), false);
		if (mc != null) {
			c = c == null ? mc : c.and(mc);
		}

		mc = fromModifier(matcher.getNotModifier(), true);
		if (mc != null) {
			c = c == null ? mc : c.and(mc);
		}

		if (matcher.getReturnType() != null) {
			mc = returns(named(matcher.getReturnType()));
			c = c == null ? mc : c.and(mc);
		}

		if (matcher.getArgsLength() > -1) {
			mc = takesArguments(matcher.getArgsLength());
			c = c == null ? mc : c.and(mc);
		}

		String[] args = matcher.getArgs();
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i] != null) {
					mc = takesArgument(i, named(args[i]));
					c = c == null ? mc : c.and(mc);
				}
			}
		}

		if (matcher.getArgsLength() >= 0) {
			mc = takesArguments(matcher.getArgsLength());
			c = c == null ? mc : c.and(mc);
		}

		if (matcher.getOverriddenFrom() != null) {
			ElementMatcher.Junction<TypeDescription> cls = ClassMatcherConvert.INSTANCE.convert(matcher.getOverriddenFrom());
			mc = isOverriddenFrom(cls);
			c = c == null ? mc : c.and(mc);
		}

		return c;
	}

	ElementMatcher.Junction<MethodDescription> fromModifier(int modifier, boolean not) {
		ElementMatcher.Junction<MethodDescription> mc = null;
		if ((modifier & ClassMatcher.MODIFIER_MASK) != 0) {
			if ((modifier & Modifier.ACC_ABSTRACT) != 0) {
				mc = isAbstract();
			}

			if ((modifier & Modifier.ACC_PUBLIC) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isPublic()) : mc.and(isPublic());
				} else {
					mc = isPublic();
				}
			}

			if ((modifier & Modifier.ACC_PRIVATE) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isPrivate()) : mc.and(isPrivate());
				} else {
					mc = isPrivate();
				}
			}

			if ((modifier & Modifier.ACC_PROTECTED) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isProtected()) : mc.and(isProtected());
				} else {
					mc = isProtected();
				}
			}

			if ((modifier & Modifier.ACC_STATIC) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isStatic()) : mc.and(isStatic());
				} else {
					mc = isStatic();
				}
			}

			if (not) {
				mc = new NegatingMatcher<>(mc);
			}
		}

		return mc;
	}
}
