package com.mawen.agent.mock.context;

import java.util.HashMap;

import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.ProgressFields;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ProgressFieldsManager {

	private ProgressFieldsManager(){}

	public static void init(Configs configs) {
		var changeListener = ProgressFields.changeListener();
		changeListener.accept(configs.getConfigs());
		configs.addChangeListener(list -> {
			var map = new HashMap<String, String>();
			for (var changeItem : list) {
				var key = changeItem.getFullName();
				if (ProgressFields.isProgressFields(key)) {
					map.put(key, changeItem.getNewValue());
				}
				changeListener.accept(map);
			}
		});
	}
}
