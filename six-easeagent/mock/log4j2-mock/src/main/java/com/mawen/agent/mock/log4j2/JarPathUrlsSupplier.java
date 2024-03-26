package com.mawen.agent.mock.log4j2;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class JarPathUrlsSupplier implements UrlSupplier{
	public static final String AGENT_SLF4J2_LIB_JAR_PATHs = "AGENT-SLF4J2-LIB-JAR-PATHS";

	@Override
	public URL[] get() {
		String dir = System.getProperty(AGENT_SLF4J2_LIB_JAR_PATHs);
		if (dir == null) {
			return new URL[0];
		}
		String[] paths = dir.split(",");
		List<URL> urls = new ArrayList<>();
		for (String path : paths) {
			if (path.trim().isEmpty()) {
				continue;
			}
			try {
				urls.add(new URL(path));
			}
			catch (MalformedURLException ignored) {
				// ignored
			}
		}
		URL[] result = new URL[urls.size()];
		urls.toArray(result);
		return result;
	}
}
