package com.mawen.agent.plugin.tools.config;

import java.util.List;

import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class NameAndSystem {
	public static final NameAndSystem INSTANCE;

	static {
		INSTANCE = new NameAndSystem();
		INSTANCE.name = Agent.getConfig(ConfigConst.SERVICE_NAME);
		INSTANCE.system = Agent.getConfig(ConfigConst.SYSTEM_NAME);
		Agent.getConfig().addChangeListener(new ConfigChangeListener() {
			@Override
			public void onChange(List<ChangeItem> list) {
				for (var changeItem : list) {
					if (ConfigConst.SERVICE_NAME.equals(changeItem.fullName())) {
						INSTANCE.name = changeItem.newValue();
					}
					if (ConfigConst.SYSTEM_NAME.equals(changeItem.fullName())) {
						INSTANCE.system = changeItem.newValue();
					}
				}
			}
		});
	}

	public static String system() {
		return INSTANCE.system;
	}

	public static String name() {
		return INSTANCE.name;
	}

	private volatile String name;
	private volatile String system;

	private NameAndSystem(){}

	public String getName() {
		return name;
	}

	public String getSystem() {
		return system;
	}
}
