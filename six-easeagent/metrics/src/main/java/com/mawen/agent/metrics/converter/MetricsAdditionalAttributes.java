package com.mawen.agent.metrics.converter;

import java.util.Map;
import java.util.function.Supplier;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.utils.AdditionalAttributes;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricsAdditionalAttributes implements Supplier<Map<String, Object>> {

	private volatile Map<String, Object> additionalAttributes;
	private volatile String serviceName = "";
	private volatile String systemName = "";

	public MetricsAdditionalAttributes(Config config) {
		ConfigUtils.bindProp(ConfigConst.SERVICE_NAME, config, Config::getString, v -> {
			this.serviceName = v;
			this.additionalAttributes = new AdditionalAttributes(this.serviceName, this.systemName).getAttributes();
		});
		ConfigUtils.bindProp(ConfigConst.SYSTEM_NAME, config, Config::getString, v -> {
			this.systemName = v;
			this.additionalAttributes = new AdditionalAttributes(this.serviceName, this.systemName).getAttributes();
		});
	}

	@Override
	public Map<String, Object> get() {
		return additionalAttributes;
	}
}
