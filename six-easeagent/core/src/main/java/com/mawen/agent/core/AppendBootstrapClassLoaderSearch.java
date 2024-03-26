package com.mawen.agent.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.google.common.io.CharStreams;
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

	private static final String DEFAULT_PATH = "/META-INF/services/";

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
		Set<String> names = findClassAnnotationAutoService(DEFAULT_PATH + AppendBootstrapLoader.class.getName());

		Map<TypeDescription, byte[]> types = types(names);

		ClassInjector.UsingInstrumentation.of(TMP_FILE, target, inst).inject(types);

		return names;
	}

	/**
	 * @since 0.0.2-SNAPSHOT
	 */
	private static Set<String> findClassAnnotationAutoService(String resourcePath) throws IOException {
		ClassLoader classLoader = AppendBootstrapClassLoaderSearch.class.getClassLoader();
		Enumeration<URL> resources = classLoader.getResources(resourcePath);

		Set<String> sets = new HashSet<>();
		while (resources.hasMoreElements()) {
			URL url = resources.nextElement();
			sets.addAll(AppendBootstrapClassLoaderSearch.readLines(url));
		}

		return sets;
	}

	/**
	 * @since 0.0.2-SNAPSHOT
	 */
	private static List<String> readLines(URL input) {
		try(InputStreamReader reader = new InputStreamReader(input.openConnection().getInputStream(), StandardCharsets.UTF_8)) {
			return CharStreams.readLines(reader);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @since 0.0.2-SNAPSHOT
	 */
	private static Map<TypeDescription, byte[]> types(Set<String> names) {
		ClassLoader loader = AppendBootstrapClassLoaderSearch.class.getClassLoader();
		ClassFileLocator locator = ClassFileLocator.ForClassLoader.of(loader);
		TypePool pool = TypePool.Default.of(locator);

		Function<String, TypeDescription> keyFunction = name -> pool.describe(name).resolve();
		Function<String, byte[]> valueFunction = name -> AppendBootstrapClassLoaderSearch.locate(locator,name);

		return names.stream().collect(Collectors.toMap(keyFunction, valueFunction));
	}

	/**
	 * @since 0.0.2-SNAPSHOT
	 */
	private static byte[] locate(ClassFileLocator locator, String name) {
		try {
			return locator.locate(name).resolve();
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	private AppendBootstrapClassLoaderSearch() {
	}

}
