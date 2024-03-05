package com.mawen.agent.report.async.log;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.async.AsyncProps;
import com.mawen.agent.report.async.AsyncReporter;
import com.mawen.agent.report.async.DefaultAsyncReporter;
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
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), OUTPUT_SERVER_V2));
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ASYNC));
		this.config = new Configs(cfg);
		configs.addChangeListener(this);

		SenderWithEncoder sender = ReporterRegistry.getSender(LOG_SENDER, configs);
		AsyncProps asyncProps = new LogAsyncProps(this.config, null);
		this.asyncReporter = DefaultAsyncReporter.create(sender,asyncProps);
		this.asyncReporter.startFlushThread();
	}

	public void report(LogData log) {
		this.asyncReporter.report(log);
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		Map<String, String> changes = filterChanges(list);
		if (changes.isEmpty()) {
			return;
		}
		this.config.updateConfigs(changes);
		this.refresh(this.config.getConfigs());
	}

	private Map<String, String> filterChanges(List<ChangeItem> list) {
		return list.stream()
				.filter(it -> it.getFullName().startsWith(LOGS)
						|| it.getFullName().startsWith(OUTPUT_SERVER_V2))
				.collect(Collectors.toMap(ChangeItem::getFullName, ChangeItem::getNewValue));
	}

	public synchronized void refresh(Map<String, String> cfg) {
		String name = cfg.get(LOG_ACCESS_SENDER_NAME);
		SenderWithEncoder sender = asyncReporter.getSender();
		if (sender != null) {
			if (StringUtils.isNotEmpty(name) && !sender.name().equals(name)) {
				try {
					sender.close();
				}
				catch (IOException e) {
					// ignored
				}
				sender = ReporterRegistry.getSender(LOG_SENDER, config);
				asyncReporter.setSender(sender);
			}
		}
		else {
			sender = ReporterRegistry.getSender(LOG_SENDER, config);
			asyncReporter.setSender(sender);
		}

		AsyncProps asyncProps = new LogAsyncProps(this.config, null);
		asyncReporter.closeFlushThread();
		asyncReporter.setPending(asyncProps.getQueuedMaxItems(), asyncProps.getQueuedMaxSize());
		asyncReporter.setMessageTimeoutNanos(messageTimeout(asyncProps.getMessageTimeout()));
		asyncReporter.startFlushThread();
	}

	protected long messageTimeout(long timeout) {
		if (timeout < 0) {
			timeout = 1000L;
		}
		return TimeUnit.MICROSECONDS.toNanos(timeout);
	}

}
