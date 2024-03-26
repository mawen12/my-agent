package com.mawen.agent;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.boot.loader.LaunchedURLClassLoader;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/18
 */
public class CompoundableClassLoader extends LaunchedURLClassLoader {

	private final Set<WeakReference<ClassLoader>> externals = new CopyOnWriteArraySet<>();

	CompoundableClassLoader(URL[] urls) {
		super(urls, Main.BOOTSTRAP_CLASS_LOADER);
	}

	public void add(ClassLoader cl) {
		if (cl != null && !Objects.equals(cl, this)) {
			externals.add(new WeakReference<>(cl));
		}
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return super.loadClass(name, resolve);
		}
		catch (ClassNotFoundException e) {
			for (WeakReference<ClassLoader> external : externals) {
				try {
					ClassLoader cl = external.get();
					if (cl == null) {
						continue;
					}
					final Class<?> aClass = cl.loadClass(name);
					if (resolve) {
						resolveClass(aClass);
					}
					return aClass;
				}
				catch (ClassNotFoundException ex) {
					// ignored
				}
			}

			throw e;
		}
	}

	@Override
	public URL findResource(String name) {
		URL url = super.findResource(name);
		if (url == null) {
			for (WeakReference<ClassLoader> external : externals) {
				try {
					ClassLoader cl = external.get();
					url = cl.getResource(name);
					if (url != null) {
						return url;
					}
				}
				catch (Exception e) {
					// ignored
				}
			}
		}
		return url;
	}
}
