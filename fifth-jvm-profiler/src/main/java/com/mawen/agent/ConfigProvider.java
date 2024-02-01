package com.mawen.agent;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public interface ConfigProvider {

	Map<String, Map<String, List<String>>> getConfig();
}
