package com.mawen.agent.mock.log4j2;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class DirUrlsSupplier implements UrlSupplier{
	public static final String LIB_DIR_ENV = "AGENT_SLF4J2-LIB-DIR";

	@Override
	public URL[] get() {
		String dir = System.getProperty(LIB_DIR_ENV);
		if (dir == null) {
			return new URL[0];
		}
		File file = new File(dir);
		if (!file.isDirectory()) {
			return new URL[0];
		}
		File[] files = file.listFiles();
		if (files == null) {
			return new URL[0];
		}
		URL[] urls = new URL[files.length];
		for (int i = 0; i < files.length; i++) {
			try {
				urls[i] = files[i].toURI().toURL();
			}
			catch (MalformedURLException e) {
				return new URL[0];
			}
		}
		return urls;
	}
}
