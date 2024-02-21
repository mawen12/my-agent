package com.mawen.agent.log4j2;

import org.apache.logging.log4j.util.Supplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class FinalClassLoaderSupplier implements Supplier<ClassLoader> {
	private static volatile ClassLoader CLASSLOADER = null;

	@Override
	public ClassLoader get() {
		return CLASSLOADER;
	}
}
