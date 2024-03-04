package com.mawen.agent.mock.context;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.ProgressFields;
import com.mawen.agent.plugin.api.config.ChangeItem;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ProgressFieldsManager {

	private ProgressFieldsManager(){}

	public static void init(Configs configs) {
		Consumer<Map<String, String>> changeListener = ProgressFields.changeListener();
		changeListener.accept(configs.getConfigs());
		configs.addChangeListener(list -> {
			Map<String, String> map = new HashMap<>();
			for (ChangeItem changeItem : list) {
				String key = changeItem.getFullName();
				if (ProgressFields.isProgressFields(key)) {
					map.put(key, changeItem.getNewValue());
				}
				changeListener.accept(map);
			}
		});
	}
}
