package com.mawen.agent.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.mawen.agent.plugin.AppendBootstrapLoader;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.ClassFileLocator;
import net.bytebuddy.dynamic.loading.ClassInjector;
import net.bytebuddy.pool.TypePool;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public final class AppendBootstrapClassLoaderSearch {

	private static final File TMP_FILE = new File(
			AccessController.doPrivileged(
					new PrivilegedAction<String>() {
						@Override
						public String run() {
							return System.getProperty("java.io.tmpdir");
						}
					}
			)
	);

	static Set<String> by(Instrumentation inst, ClassInjector.UsingInstrumentation.Target target) throws IOException {
		Set<String> names = findClassAnnotationAutoService(AppendBootstrapLoader.class);
		ClassInjector.UsingInstrumentation.of(TMP_FILE, target, inst).inject(types(names));
		return names;
	}

	private static Set<String> findClassAnnotationAutoService(Class<?> clazz) throws IOException {
		final ClassLoader loader = AppendBootstrapClassLoaderSearch.class.getClassLoader();

		return FluentIterable.from(Collections.list(loader.getResources("/META-INF/services/" + clazz.getName())))
				.transform(input -> {
					try {
						URLConnection connection = input.openConnection();
						InputStream inputStream = connection.getInputStream();
						return new InputStreamReader(inputStream, StandardCharsets.UTF_8);
					}
					catch (IOException e) {
						throw new IllegalStateException(e);
					}
				})
				.transformAndConcat((Function<InputStreamReader, Iterable<String>>) input -> {
					try {
						return CharStreams.readLines(input);
					}
					catch (IOException e) {
						throw new IllegalStateException(e);
					}
					finally {
						Closeables.closeQuietly(input);
					}
				})
				.toSet();
	}

	private static Map<TypeDescription, byte[]> types(Set<String> names) {
		ClassLoader loader = AppendBootstrapClassLoaderSearch.class.getClassLoader();
		ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
		TypePool pool = TypePool.Default.of(locator);

		return Maps.transformValues(Maps.uniqueIndex(names, input -> pool.describe(input).resolve()),
				input -> {
					try {
						return locator.locate(input).resolve();
					}
					catch (IOException e) {
						throw new IllegalStateException(e);
					}
				});
	}

	private AppendBootstrapClassLoaderSearch() {
	}

}