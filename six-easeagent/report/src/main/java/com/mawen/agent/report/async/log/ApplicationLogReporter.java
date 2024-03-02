package com.mawen.agent.report.async.log;

import java.util.List;
import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.async.AsyncReporter;
import com.mawen.agent.report.plugin.ReporterRegistry;
import com.mawen.agent.report.sender.SenderWithEncoder;
import io.opentelemetry.sdk.logs.data.LogData;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class ApplicationLogReporter implements ConfigChangeListener {

	Config config;
	AsyncReporter<LogData> asyncReporter;

	public ApplicationLogReporter(Config configs) {
		Map<String, String> cfg = ConfigUtils.extractByPrefix(configs.getConfigs(), LOGS);
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(),OUTPUT_SERVER_V2));
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(),LOG_ASYNC));
		this.config = new Configs(cfg);
		configs.addChangeListener(this);

		SenderWithEncoder sender = ReporterRegistry.getSender(LOG_SENDER, configs);
		AsyncProps asyncProps = new LogAsyncProps(this.config, null);
		this.asyncReporter = DefaultAsyncReporter;
		this.asyncReporter.startFlushThread();
	}

	@Override
	public void onChange(List<ChangeItem> list) {

	}
}
