package com.mawen.agent.plugin.matcher.loader;

import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class ClassLoaderMatcher implements IClassLoaderMatcher{
	public static final String BOOTSTRAP_NAME = "bootstrap";
	public static final String EXTERNAL_NAME = "external";
	public static final String SYSTEM_NAME = "system";
	public static final String AGENT_NAME = "agent";

	// predefined classloader name and matcher
	public static final ClassLoaderMatcher ALL = new ClassLoaderMatcher("all");
	public static final ClassLoaderMatcher BOOTSTRAP = new ClassLoaderMatcher(BOOTSTRAP_NAME);
	public static final ClassLoaderMatcher EXTERNAL = new ClassLoaderMatcher(EXTERNAL_NAME);
	public static final ClassLoaderMatcher SYSTEM = new ClassLoaderMatcher(SYSTEM_NAME);
	public static final ClassLoaderMatcher AGENT = new ClassLoaderMatcher(AGENT_NAME);

	// classloader class name or Predefined names
	String classLoaderName;

	public ClassLoaderMatcher(String classLoaderName) {
		if (StringUtils.isEmpty(classLoaderName)) {
			this.classLoaderName = BOOTSTRAP_NAME;
		} else {
			this.classLoaderName = classLoaderName;
		}
	}

	@Override
	public String getClassLoaderName() {
		return classLoaderName;
	}

	@Override
	public IClassLoaderMatcher negate() {
		return new NegateClassLoaderMatcher(this);
	}

	@Override
	public int hashCode() {
		return this.classLoaderName.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof ClassLoaderMatcher)) {
			return false;
		}
		return this.classLoaderName.equals(((ClassLoaderMatcher) o).getClassLoaderName());
	}
}
