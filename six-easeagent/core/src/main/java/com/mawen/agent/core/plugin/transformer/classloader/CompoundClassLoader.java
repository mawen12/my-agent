package com.mawen.agent.core.plugin.transformer.classloader;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class CompoundClassLoader {

	private static final Logger log = LoggerFactory.getLogger(CompoundClassLoader.class);

	private static final Cache<ClassLoader, Boolean> CACHE = CacheBuilder.newBuilder().weakKeys().build();

	public static boolean checkClassloaderExist(ClassLoader loader) {
		if (CACHE.getIfPresent(loader) == null) {
			CACHE.put(loader, true);
			return false;
		}
		return true;
	}

	public static ClassLoader compound(ClassLoader parent, ClassLoader external) {
		if (external == null || checkClassloaderExist(external)) {
			return parent;
		}

		try {
			parent.getClass().getDeclaredMethod("add", ClassLoader.class).invoke(parent, external);
		}
		catch (Exception e) {
			log.warn("{}, this may be a bug if it was running in production", e.toString());
		}
		return parent;
	}
}
