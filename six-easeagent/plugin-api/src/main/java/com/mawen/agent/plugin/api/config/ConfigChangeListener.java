package com.mawen.agent.plugin.api.config;

import java.util.List;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface ConfigChangeListener {
	void onChange(List<ChangeItem> list);
}
