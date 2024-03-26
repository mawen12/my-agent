package com.mawen.agent.log4j2;

import java.util.Iterator;
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
			FinalClassLoaderSupplier supplier = new FinalClassLoaderSupplier();
			ClassLoader classLoader = supplier.get();
			if (classLoader != null) {
				return classLoader;
			}
			ServiceLoader<ClassLoaderSupplier> loader = ServiceLoader.load(ClassLoaderSupplier.class);
			Iterator<ClassLoaderSupplier> iterator = loader.iterator();
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
