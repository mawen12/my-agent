package com.mawen.agent.log4j2;

import java.util.ServiceLoader;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public interface ClassLoaderSupplier {

	ClassLoader get();

	class ClassLoaderHolder implements ClassLoaderSupplier {
		@Override
		public ClassLoader get() {
			var supplier = new FinalClassLoaderSupplier();
			var classLoader = supplier.get();
			if (classLoader != null) {
				return classLoader;
			}
			var loader = ServiceLoader.load(ClassLoaderSupplier.class);
			var iterator = loader.iterator();
			while (iterator.hasNext()) {
				classLoader = iterator.next().get();
				if (classLoader != null) {
					return classLoader;
				}
			}
			return null;
		}
	}
}
