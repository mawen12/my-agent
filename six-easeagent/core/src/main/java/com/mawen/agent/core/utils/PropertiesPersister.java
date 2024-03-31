package com.mawen.agent.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public interface PropertiesPersister {

	void load(Properties props, InputStream is) throws IOException;

	void load(Properties props, Reader reader) throws IOException;

	void loadFromXml(Properties props, InputStream is) throws IOException;
}
