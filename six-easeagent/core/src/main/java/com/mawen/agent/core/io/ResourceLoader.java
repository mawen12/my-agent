package com.mawen.agent.core.io;

import com.mawen.agent.core.utils.ResourceUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/31
 */
public interface ResourceLoader {

	String CLASS_URL_PREFIX = ResourceUtils.CLASSPATH_URL_PREFIX;

	Resource getResource(String location);

	ClassLoader getClassLoader();
}
