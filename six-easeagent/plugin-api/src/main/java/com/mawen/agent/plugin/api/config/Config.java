package com.mawen.agent.plugin.api.config;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Config {

	boolean hasPath(String path);

	String getString(String name);

	String getString(String name, String defVal);

	Integer getInt(String name);

	Integer getInt(String name, int defVal);

	Boolean getBoolean(String name);

	Boolean getBoolean(String name, boolean defVal);

	Boolean getBooleanNullForUnset(String name);

	Double getDouble(String name);

	Double getDouble(String name, double defVal);

	Long getLong(String name);

	Long getLong(String name, long defVal);

	List<String> getStringList(String name);

	Set<String> keySet();

	Map<String, String> getConfigs();
}
