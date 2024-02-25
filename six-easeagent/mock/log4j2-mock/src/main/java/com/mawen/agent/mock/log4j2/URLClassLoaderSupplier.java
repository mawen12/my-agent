package com.mawen.agent.mock.log4j2;

import java.net.URLClassLoader;
import java.util.Objects;

import com.mawen.agent.log4j2.ClassLoaderSupplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class URLClassLoaderSupplier implements ClassLoaderSupplier {

	@Override
	public ClassLoader get() {
		JarUrlsSupplier jarUrlsSupplier = new JarUrlsSupplier(new UrlSupplier[] {
				new AllUrlsSupplier(), new DirUrlsSupplier(), new JarPathUrlsSupplier()
		});
		return new URLClassLoader(Objects.requireNonNull(jarUrlsSupplier.get(), "urls must not be null"));
	}

}
