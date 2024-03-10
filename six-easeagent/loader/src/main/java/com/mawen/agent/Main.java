package com.mawen.agent;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import org.springframework.boot.loader.LaunchedURLClassLoader;
import org.springframework.boot.loader.archive.JarFileArchive;

/**
 * Agent 总入口
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/21
 */
public class Main {
	private static final ClassLoader BOOTSTRAP_CLASS_LOADER = null;
	private static final String LIB = "lib/";
	private static final String BOOTSTRAP = "boot/";
	private static final String SLf4j2 = "log4j2/";
	private static final String PLUGINS = "plugins/";
	private static final String LOGGING_PROPERTY = "Logging-Property";
	private static final String AGENT_LOG_CONF = "agent.log.conf";
	private static final String AGENT_LOG_CONF_ENV_KEY = "AGENT_LOG_CONF";
	private static final String DEFAULT_T_AGENT_LOG_CONF = "agent-log4j2.xml";
	private static ClassLoader loader;

	public static void premain(final String args, final Instrumentation inst) throws Exception {
		var jar = getArchiveFileContains();
		final var archive = new JarFileArchive(jar);

		// custom classloader
		var urls = nestArchiveUrls(archive, LIB);
		urls.addAll(nestArchiveUrls(archive, PLUGINS));
		urls.addAll(nestArchiveUrls(archive, SLf4j2));
		var p = new File(jar.getParent() + File.separator + "plugins");
		if (p.exists()) {
			urls.addAll(directoryPluginUrls(p));
		}

		loader = new CompoundableClassLoader(urls.toArray(new URL[0]));

		// install bootstrap jar
		final var bootUrls = nestArchiveUrls(archive, BOOTSTRAP);
		bootUrls.forEach(url -> installBootstrapJar(url, inst));

		// init slf4j2 dir
		initAgentSlf4j2Dir(archive, loader);

		// init slf4j mdc and call Bootstrap#premain
		final var attributes = archive.getManifest().getMainAttributes();
		final var loggingProperty = attributes.getValue(LOGGING_PROPERTY);
		final var bootstrap = attributes.getValue("Bootstrap-Class");
		switchLoggingProperty(loader, loggingProperty, () -> {
			initAgentSlf4jMDC(loader);
			loader.loadClass(bootstrap)
					.getMethod("premain", String.class, Instrumentation.class, String.class)
					.invoke(null, args, inst, jar.getPath());
			return null;
		});
	}

	private static File getArchiveFileContains() throws URISyntaxException {
		final var protectionDomain = Main.class.getProtectionDomain();
		final var codeSource = protectionDomain.getCodeSource();
		final var location = (codeSource == null ? null : codeSource.getLocation().toURI());
		final var path = (location == null ? null : location.getSchemeSpecificPart());

		if (path == null) {
			throw new IllegalStateException("Unable to determine code source archive");
		}

		final var root = new File(path);
		if (!root.exists() || root.isDirectory()) {
			throw new IllegalStateException("Unable to determine code source archive from " + root);
		}
		return root;
	}

	private static ArrayList<URL> nestArchiveUrls(JarFileArchive archive, String prefix) throws IOException {
		var archives = Lists.newArrayList(
				archive.getNestedArchives(entry -> !entry.isDirectory() && entry.getName().startsWith(prefix),
						entry -> true
				));

		final var urls = new ArrayList<URL>(archives.size());

		archives.forEach(item -> {
			try {
				urls.add(item.getUrl());
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});

		return urls;
	}

	private static ArrayList<URL> directoryPluginUrls(File directory) {
		if (!directory.isDirectory()) {
			return new ArrayList<>();
		}

		var files = directory.listFiles();
		if (files == null) {
			return new ArrayList<>();
		}

		final var urls = new ArrayList<URL>(files.length);

		Arrays.stream(files).forEach(item -> {
			if (!item.getName().endsWith("jar")) {
				return;
			}
			try {
				var pUrl = item.toURI().toURL();
				urls.add(pUrl);
			}
			catch (MalformedURLException e) {
				e.printStackTrace();
			}
		});
		return urls;
	}


	private static void installBootstrapJar(URL url, Instrumentation inst) {
		try {
			var file = JarUtils.getNestedJarFile(url);
			inst.appendToBootstrapClassLoaderSearch(file);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initAgentSlf4j2Dir(JarFileArchive archive, final ClassLoader bootstrapLoader) throws Exception {
		final var slf4j2Urls = nestArchiveUrls(archive, SLf4j2).toArray(new URL[0]);
		final var slf4j2Loader = new URLClassLoader(slf4j2Urls, null);
		var classLoaderSupplier = bootstrapLoader.loadClass("com.mawen.agent.log4j2.FinalClassLoaderSupplier");
		var field = classLoaderSupplier.getDeclaredField("CLASSLOADER");
		field.set(null, slf4j2Loader);
	}

	private static void switchLoggingProperty(ClassLoader loader, String hostKey, Callable<Void> callable) throws Exception {
		final var t = Thread.currentThread();
		final var ccl = t.getContextClassLoader();

		t.setContextClassLoader(loader);

		// get config from system properties
		final var host = System.getProperty(hostKey);
		final var agent = getLogConfigPath();

		// Redirect config of host to agent
		System.setProperty(hostKey, agent);

		try {
			callable.call();
		}
		finally {
			t.setContextClassLoader(ccl);
			// Recovery host configuration
			if (host == null) {
				System.getProperties().remove(hostKey);
			}
			else {
				System.setProperty(hostKey, host);
			}
		}
	}

	private static String getLogConfigPath() {
		var logConfigPath = System.getProperty(AGENT_LOG_CONF);
		if (Strings.isNullOrEmpty(logConfigPath)) {
			logConfigPath = System.getenv(AGENT_LOG_CONF_ENV_KEY);
		}
		// if not set, use default
		if (Strings.isNullOrEmpty(logConfigPath)) {
			logConfigPath = DEFAULT_T_AGENT_LOG_CONF;
		}
		return logConfigPath;
	}

	private static void initAgentSlf4jMDC(ClassLoader loader) {
		// init slf4j MDC for inner agent
		Class<?> mdcClass;
		try {
			mdcClass = loader.loadClass("org.slf4j.MDC");
			// just make a reference to mdcClass avoiding JIT remove
			mdcClass.getMethod("remove", String.class)
					.invoke(null, "Agent");
		}
		catch (Exception e) {
			// ignored
		}
	}

	public static class CompoundableClassLoader extends LaunchedURLClassLoader {
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
				for (var external : externals) {
					try {
						var cl = external.get();
						if (cl == null) {
							continue;
						}
						final var aClass = cl.loadClass(name);
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
			var url = super.findResource(name);
			if (url == null) {
				for (var external : externals) {
					try {
						var cl = external.get();
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

}
