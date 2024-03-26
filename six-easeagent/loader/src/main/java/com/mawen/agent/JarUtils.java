package com.mawen.agent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/21
 */
public class JarUtils {

	private JarUtils() {
	}

	private static final int EOF = -1;
	private static final int BUFFER_SIZE = 4096;

	private static final File TEMP_FILE = new File(AccessController.doPrivileged(
			new PrivilegedAction<String>() {
				@Override
				public String run() {
					return System.getProperty("java.io.tmpdir") + File.separator + "agent" + File.separator;
				}
			}
	));

	private static SoftReference<Map<String, JarFile>> fileCache;

	static {
		fileCache = new SoftReference<>(new ConcurrentHashMap<>());
	}

	private static final String SEPARATOR = "!/";
	private static final String FILE_PROTOCOL = "file:";

	static JarFile getNestedJarFile(URL url) throws IOException {
		StringSequence spec = new StringSequence(url.getFile());
		Map<String, JarFile> cache = fileCache.get();
		JarFile jarFile = (cache != null) ? cache.get(spec.toString()) : null;

		if (jarFile != null) {
			return jarFile;
		} else {
			jarFile = getRootJarFileFromUrl(url);
		}

		int separator;
		int index = indexOfRootSpec(spec);
		if (index == -1) {
			return null;
		}
		StringSequence entryName;
		if ((separator = spec.indexOf(SEPARATOR, index)) > 0) {
			entryName = spec.subSequence(index, separator);
		} else {
			entryName = spec.subSequence(index);
		}
		JarEntry jarEntry = jarFile.getJarEntry(entryName.toString());
		if (jarEntry == null) {
			return null;
		}
		try (InputStream input = jarFile.getInputStream(jarEntry)) {
			File output = createTempJarFile(input, jarEntry.getName());
			jarFile = new JarFile(output);
			addToRootFileCache(url.getPath(),jarFile);
		}

		return jarFile;
	}

	public static void copy(InputStream input, OutputStream output) throws IOException {
		int n;
		final byte[] buffer = new byte[BUFFER_SIZE];
		while (EOF != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}

	public static JarFile getRootJarFileFromUrl(URL url) throws IOException {
		String name = getRootJarFileName(url);
		return getRootJarFile(name);
	}

	public static String getRootJarFileName(URL url) throws MalformedURLException {
		String spec = url.getFile();
		int separatorIndex = spec.indexOf(SEPARATOR);
		if (separatorIndex == -1) {
			throw new MalformedURLException("Jar URL does not contain !/ separator");
		}
		return spec.substring(0, separatorIndex);
	}

	private static JarFile getRootJarFile(String name) {
		try {
			if (!name.startsWith(FILE_PROTOCOL)) {
				throw new IllegalArgumentException("Not a file URL");
			}
			File file = new File(URI.create(name));
			Map<String, JarFile> cache = fileCache.get();
			JarFile result = (cache != null) ? cache.get(name) : null;
			if (result == null) {
				result = new JarFile(file);
				addToRootFileCache(name,result);
			}
			return result;
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static int indexOfRootSpec(StringSequence file) {
		int separatorIndex = file.indexOf(SEPARATOR);
		if (separatorIndex < 0) {
			return -1;
		}
		return separatorIndex + SEPARATOR.length();
	}

	private static File createTempJarFile(InputStream input, String outputName) throws IOException {
		File dir;
		String fName = (new File(outputName)).getName();
		if (fName.length() < outputName.length()) {
			String localDir = outputName.substring(0, outputName.length() - fName.length());
			Path path = Paths.get(TEMP_FILE.getPath() + File.separator + localDir);
			dir = Files.createDirectories(path).toFile();
		} else {
			dir = TEMP_FILE;
		}
		File f = new File(dir, fName);
		f.deleteOnExit();
		try (FileOutputStream outputStream = new FileOutputStream(f)) {
			copy(input, outputStream);
		}

		return f;
	}

	static void addToRootFileCache(String fileName, JarFile jarFile) {
		Map<String, JarFile> cache = fileCache.get();
		if (cache == null) {
			cache = new ConcurrentHashMap<>(8);
			fileCache = new SoftReference<>(cache);
		}
		cache.put(fileName, jarFile);
	}
}
