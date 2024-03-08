package com.mawen.agent.core.plugin.matcher;

import com.mawen.agent.plugin.asm.Modifier;
import com.mawen.agent.plugin.matcher.ClassMatcher;
import com.mawen.agent.plugin.matcher.IClassMatcher;
import com.mawen.agent.plugin.matcher.operator.AndClassMatcher;
import com.mawen.agent.plugin.matcher.operator.NegateClassMatcher;
import com.mawen.agent.plugin.matcher.operator.OrClassMatcher;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.NegatingMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ClassMatcherConvert implements Converter<IClassMatcher, ElementMatcher.Junction<TypeDescription>> {

	public static final ClassMatcherConvert INSTANCE = new ClassMatcherConvert();

	@Override
	public ElementMatcher.Junction<TypeDescription> convert(IClassMatcher source) {
		if (source == null) {
			return null;
		}

		if (source instanceof AndClassMatcher andMatcher) {
			ElementMatcher.Junction<TypeDescription> leftMatcher = this.convert(andMatcher.getLeft());
			ElementMatcher.Junction<TypeDescription> rightMatcher = this.convert(andMatcher.getRight());
			return leftMatcher.and(rightMatcher);
		}
		else if (source instanceof OrClassMatcher orMatcher) {
			ElementMatcher.Junction<TypeDescription> leftMatcher = this.convert(orMatcher.getLeft());
			ElementMatcher.Junction<TypeDescription> rightMatcher = this.convert(orMatcher.getRight());
			return leftMatcher.or(rightMatcher);
		}
		else if (source instanceof NegateClassMatcher matcher) {
			ElementMatcher.Junction<TypeDescription> notMatcher = this.convert(matcher.getMatcher());
			return new NegatingMatcher<>(notMatcher);
		}

		if (!(source instanceof ClassMatcher)) {
			return null;
		}

		return this.convert((ClassMatcher)source);
	}

	private ElementMatcher.Junction<TypeDescription> convert(ClassMatcher matcher) {
		ElementMatcher.Junction<TypeDescription> c;
		switch (matcher.getMatchType()) {
			case NAMED:
				c = named(matcher.getName());
				break;
			case SUPER_CLASS:
			case INTERFACE:
				c = hasSuperType(named(matcher.getName()));
				break;
			case ANNOTATION:
				c = isAnnotatedWith(named(matcher.getName()));
				break;
			default:
				return null;
		}

		ElementMatcher.Junction<TypeDescription> mc = fromModifier(matcher.getModifier(), false);
		if (mc != null) {
			c = c.and(c);
		}
		mc = fromModifier(matcher.getNotModifier(), true);
		if (mc != null) {
			c = c.and(mc);
		}

		return c;
	}

	ElementMatcher.Junction<TypeDescription> fromModifier(int modifier, boolean not) {
		ElementMatcher.Junction<TypeDescription> mc = null;
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

			if ((modifier & Modifier.ACC_INTERFACE) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isInterface()) : mc.and(isInterface());
				} else {
					mc = isInterface();
				}
			}

			if ((modifier & Modifier.ACC_PROTECTED) != 0) {
				if (mc != null) {
					mc = not ? mc.or(isProtected()) : mc.and(isProtected());
				} else {
					mc = isProtected();
				}
			}

			if (not) {
				mc = new NegatingMatcher<>(mc);
			}
		}
		return mc;
	}
}
