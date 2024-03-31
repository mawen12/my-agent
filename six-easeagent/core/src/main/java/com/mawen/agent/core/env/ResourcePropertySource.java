package com.mawen.agent.core.env;

import java.util.Map;

import com.mawen.agent.core.io.EncodedResource;
import com.mawen.agent.core.io.Resource;
import com.mawen.agent.core.io.support.PropertiesLoaderUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class ResourcePropertySource extends PropertiesPropertySource {

	private final String resourceName;

	public ResourcePropertySource(String name, EncodedResource resource) {
		super(name, PropertiesLoaderUtils.loadProperties(resource));
		this.resourceName = getNameForResource(resource.getResource());
	}

	public ResourcePropertySource(EncodedResource resource) {
		super(getNameForResource(resource.getResource()), PropertiesLoaderUtils.loadProperties(resource));
		this.resourceName = null;
	}

	public ResourcePropertySource(String name, Resource resource) {
		super(name, PropertiesLoaderUtils.loadProperties(new EncodedResource(resource)));
		this.resourceName = getNameForResource(resource);
	}

	public ResourcePropertySource(Resource resource) {
		super(getNameForResource(resource), PropertiesLoaderUtils.loadProperties(new EncodedResource(resource)));
		this.resourceName = null;
	}

	public ResourcePropertySource(String name, String location, ClassLoader classLoader) {
		this(name, new DefaultResourceLoader(classLoader).getResource(location));
	}

	public ResourcePropertySource(String location, ClassLoader classLoader) {
		this(new DefaultResourceLoader(classLoader).getResource(location));
	}

	public ResourcePropertySource(String name, String location) {
		this(name, new DefaultResourceLoader().getResource(location));
	}

	public ResourcePropertySource(String location) {
		this(new DefaultResourceLoader().getResource(location));
	}

	private ResourcePropertySource(String name, String resourceName, Map<String, Object> source) {
		super(name, source);
		this.resourceName = resourceName;
	}

	private static String getNameForResource(Resource resource) {
		String name = resource.getDescription();
		if (StringUtils.isBlank(name)) {
			name = resource.getClass().getSimpleName() + "@" + System.identityHashCode(resource);
		}
		return name;
	}
}
