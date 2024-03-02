package com.mawen.agent.report;

import com.mawen.agent.config.AutoRefreshConfigItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.report.encoder.span.GlobalExtrasSupplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class GlobalExtractor implements GlobalExtrasSupplier {

	static volatile GlobalExtractor instance;

	final AutoRefreshConfigItem<String> serviceName;
	final AutoRefreshConfigItem<String> systemName;

	public static GlobalExtractor getInstance(Config configs) {
		if (instance == null) {
			synchronized (GlobalExtractor.class) {
				if (instance != null) {
					return instance;
				}
				instance = new GlobalExtractor(configs);
			}
		}
		return instance;
	}

	private GlobalExtractor(Config configs) {
		serviceName = new AutoRefreshConfigItem<>(configs, ConfigConst.SERVICE_NAME, Config::getString);
		systemName = new AutoRefreshConfigItem<>(configs, ConfigConst.SYSTEM_NAME, Config::getString);
	}

	@Override
	public String service() {
		return serviceName.getValue();
	}

	@Override
	public String system() {
		return systemName.getValue();
	}
}
