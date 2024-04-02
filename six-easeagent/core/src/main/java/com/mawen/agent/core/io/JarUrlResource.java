package com.mawen.agent.core.io;

import java.net.MalformedURLException;

import org.springframework.core.io.UrlResource;
import org.springframework.util.ResourceUtils;

/**
 * <pre>{@code
 * 	UrlResource resource = new JarUrlResource(ResourceUtils.FILE_URL_PREFIX + pathToUse + ResourceUtils.JAR_URL_SEPARATOR + "agent.properties");
 * 	PropertySource propertySource = new ResourcePropertySource(urlResource);
 * 	System.out.println(propertySource.getProperty("name"));
 * }</pre>
 *
 * <pre>{@code
 * 	UrlResource resource = new JarUrlResource(pathToUse, "agent.properties");
 * 	PropertySource propertySource = new ResourcePropertySource(urlResource);
 * 	System.out.println(propertySource.getProperty("name"));
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/4/1
 */
public class JarUrlResource extends UrlResource {

	public JarUrlResource(String path) throws MalformedURLException {
		super(ResourceUtils.URL_PROTOCOL_JAR, path);
	}

	public JarUrlResource(String jarPath, String filePathInJar) throws MalformedURLException {
		super(ResourceUtils.URL_PROTOCOL_JAR, ResourceUtils.FILE_URL_PREFIX + jarPath + ResourceUtils.JAR_URL_SEPARATOR + filePathInJar);
	}
}
