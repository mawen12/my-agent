package com.mawen.agent.plugin.tools.loader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.agent.plugin.utils.common.WeakConcurrentMap;

/**
 * if there are classes with the same classname in user classloaders
 * and agent classloader, to avoid class cast exception in plugins,
 * only load these classes by user classloaders in plugin context.
 * Other related plugin classes loaded by this classloader.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class AgentHelperClassLoader extends URLClassLoader {
	private static final ConcurrentHashMap<URL, URL> helpUrls = new ConcurrentHashMap<>();
	private static final WeakConcurrentMap<ClassLoader, AgentHelperClassLoader> helpLoaders = new WeakConcurrentMap<>();

	private final URLClassLoader agentClassLoader;

	public AgentHelperClassLoader(URL[] urls, ClassLoader parent, URLClassLoader agentClassLoader) {
		super(urls, parent);
		this.agentClassLoader = agentClassLoader;
	}

	public static AgentHelperClassLoader getClassLoader(ClassLoader parent, URLClassLoader agent) {
		var help = helpLoaders.getIfPresent(parent);
		if (help != null) {
			return help;
		}
		else {
			URL[] urls;
			if (helpUrls.isEmpty()) {
				urls = new URL[0];
			} else {
				urls = helpUrls.keySet().toArray(new URL[1]);
			}
			help = new AgentHelperClassLoader(urls, parent, agent);
			if (helpLoaders.putIfProbablyAbsent(parent, help) == null) {
				return help;
			} else {
				return helpLoaders.getIfPresent(parent);
			}
		}
	}

	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		try {
			return super.loadClass(name, resolve);
		}
		catch (ClassNotFoundException e) {
			try {
				final var aClass = this.agentClassLoader.loadClass(name);
				if (resolve) {
					resolveClass(aClass);
				}
				return aClass;
			}
			catch (ClassNotFoundException ignored) {
				// ignored
			}
			throw e;
		}
	}

	@Override
	public URL findResource(String name) {
		var url = super.findResource(name);
		try {
			url = this.agentClassLoader.getResource(name);
			if (url != null) {
				return url;
			}
		}
		catch (Exception ignored) {
			// ignored
		}
		return url;
	}
}
