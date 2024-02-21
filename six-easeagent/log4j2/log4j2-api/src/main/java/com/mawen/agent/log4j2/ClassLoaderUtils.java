package com.mawen.agent.log4j2;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class ClassLoaderUtils {

	public static URL[] getAllURLs(ClassLoader classLoader) {
		return getAllURLs(classLoader,null);
	}

	public static URL[] getAllURLs(ClassLoader classLoader, Function<URL, Boolean> filter) {
		List<URL> list = new ArrayList<>();
		Function<URL, Boolean> f = (filter != null) ? filter : url -> true;
		try {
			Enumeration<URL> enumeration = classLoader.getResources("META-INF");
			fillUrls(list,enumeration,f);
			Enumeration<URL> enumeration2 = classLoader.getResources("");
			fillUrls(list,enumeration2,f);
		}
		catch (IOException e) {
			// ignored
		}
		return list.toArray(new URL[0]);
	}

	private static void fillUrls(List<URL> list, Enumeration<URL> enumeration, @Nonnull Function<URL, Boolean> filter) throws IOException {
		while (enumeration.hasMoreElements()) {
			URL url = enumeration.nextElement();
			URLConnection urlConnection = url.openConnection();
			URL resultUrl = url;
			if (urlConnection instanceof JarURLConnection) {
				resultUrl = ((JarURLConnection) urlConnection).getJarFileURL();
			}
			if (list.contains(resultUrl)) {
				continue;
			}
			if (filter(filter, resultUrl)) {
				list.add(resultUrl);
			}
		}
	}

	private static boolean filter(Function<URL, Boolean> filter, URL url) {
		Boolean f = filter.apply(url);
		return  f != null && f;
	}
}
