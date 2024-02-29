package com.mawen.agent.report.async.log;

import java.util.List;
import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.async.AsyncReporter;
import com.mawen.agent.report.sender.SenderWithEncoder;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class AccessLogReporter implements ConfigChangeListener {

	Config config;
	AsyncReporter<AccessLogInfo> asyncReporter;

	public AccessLogReporter(Config configs) {
		Map<String, String> cfg = ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ACCESS);
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), OUTPUT_SERVER_V2));
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(),LOG_ASYNC));

		this.config = new Configs(cfg);
		configs.addChangeListener(this);

		AsyncProps asyncProperties = new LogAsyncProps(this.config, LOG_ACCESS);
		SenderWithEncoder sender = ReporterRegistry.getSender();
	}

	@Override
	public void onChange(List<ChangeItem> list) {

	}
}
