package com.mawen.agent.plugin.jdbc.support.compress;

import java.util.Map;
import java.util.function.Consumer;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.utils.common.HostAddress;
import com.mawen.agent.plugin.utils.common.JsonUtil;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public class MD5ReporterConsumer implements Consumer<Map<String, String>> {

	private final IPluginConfig config;
	private Reporter reporter;

	public MD5ReporterConsumer() {
		this.config = AutoRefreshPluginConfigRegistry.getOrCreate(ConfigConst.OBSERVABILITY,
				ConfigConst.Namespace.MD5_DICTIONARY,
				ConfigConst.PluginID.METRIC);
		this.reporter = Agent.metricReporter(config);
	}

	@Override
	public void accept(Map<String, String> map) {
		if (!this.config.enabled()) {
			return;
		}

		for (Map.Entry<String, String> entry : map.entrySet()) {
			MD5DictionaryItem item = new MD5DictionaryItem(System.currentTimeMillis(),
					"application",
					HostAddress.localhost(),
					HostAddress.getHostIpv4(),
					"",
					"demo-service",
					"demo-system",
					"md5-dictionary",
					"",
					"",
					entry.getKey(),
					entry.getValue()
			);

			String json = JsonUtil.toJson(item);
			this.reporter.report(json);
		}

	}
}
