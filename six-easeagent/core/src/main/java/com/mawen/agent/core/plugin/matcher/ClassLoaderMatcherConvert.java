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
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class ClassLoaderMatcherConvert implements Converter<IClassLoaderMatcher, ElementMatcher<ClassLoader>> {
	public static final ClassLoaderMatcherConvert INSTANCE = new ClassLoaderMatcherConvert();

	private static final ElementMatcher<ClassLoader> agentLoaderMatcher = is(Bootstrap.class.getClassLoader())
			.or(is(FinalClassLoaderSupplier.CLASSLOADER));

	@Override
	public ElementMatcher<ClassLoader> convert(IClassLoaderMatcher source) {
		boolean negate = false;
		ElementMatcher<ClassLoader> matcher;
		if (source instanceof NegateClassLoaderMatcher) {
			negate = true;
			source = source.negate();
		}

		if (ALL.equals(source)) {
			matcher = any();
		} else {
			switch (source.getClassLoaderName()) {
				case BOOTSTRAP_NAME:
					matcher = ElementMatchers.isBootstrapClassLoader();
					break;
				case EXTERNAL_NAME:
					matcher = ElementMatchers.isExtensionClassLoader();
					break;
				case SYSTEM_NAME:
					matcher = ElementMatchers.isSystemClassLoader();
					break;
				case AGENT_NAME:
					matcher = agentLoaderMatcher;
					break;
				default:
					matcher = new NameMatcher(source.getClassLoaderName());
					break;
			}
		}

		if (negate) {
			return not(matcher);
		} else {
			return matcher;
		}
	}

	static class NameMatcher implements ElementMatcher<ClassLoader> {

		final String className;

		public NameMatcher(String className) {
			this.className = className;
		}

		@Override
		public boolean matches(ClassLoader target) {
			if (target == null) {
				return this.className == null;
			}
			return this.className.equals(target.getClass().getCanonicalName());
		}
	}
}
