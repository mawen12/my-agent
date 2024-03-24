package com.mawen.agent.core.plugin.matcher;

import com.mawen.agent.core.Bootstrap;
import com.mawen.agent.log4j2.FinalClassLoaderSupplier;
import com.mawen.agent.plugin.matcher.loader.IClassLoaderMatcher;
import com.mawen.agent.plugin.matcher.loader.NegateClassLoaderMatcher;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;

import static com.mawen.agent.plugin.matcher.loader.ClassLoaderMatcher.*;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * 将 {@link IClassLoaderMatcher} 转换为 ByteBuddy 中的 {@link ElementMatcher}
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public enum ClassLoaderMatcherConvert implements Converter<IClassLoaderMatcher, ElementMatcher<ClassLoader>> {
	INSTANCE;

	@Override
	public ElementMatcher<ClassLoader> convert(IClassLoaderMatcher source) {
		var negate = false;
		ElementMatcher<ClassLoader> matcher;
		if (source instanceof NegateClassLoaderMatcher) {
			negate = true;
			source = source.negate();
		}

		if (ALL.equals(source)) {
			matcher = any();
		}
		else {
			matcher = switch (source.getClassLoaderName()) {
				case BOOTSTRAP_NAME -> ElementMatchers.isBootstrapClassLoader();
				case EXTERNAL_NAME -> ElementMatchers.isExtensionClassLoader();
				case SYSTEM_NAME -> ElementMatchers.isSystemClassLoader();
				case AGENT_NAME -> is(Bootstrap.class.getClassLoader())
						.or(is(FinalClassLoaderSupplier.CLASSLOADER));
				default -> new NameMatcher(source.getClassLoaderName());
			};
		}

		return negate ? not(matcher) : matcher;
	}

	record NameMatcher(String className) implements ElementMatcher<ClassLoader> {

		@Override
		public boolean matches(ClassLoader target) {
			return target == null ? this.className == null : this.className.equals(target.getClass().getCanonicalName());
		}
	}
}
