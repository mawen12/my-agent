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
import com.mawen.agent.report.async.AsyncReporter;
import com.mawen.agent.report.async.DefaultAsyncReporter;
import com.mawen.agent.report.plugin.ReporterRegistry;
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
		var cfg = ConfigUtils.extractByPrefix(configs.getConfigs(), LOGS);
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), OUTPUT_SERVER_V2));
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ASYNC));
		this.config = new Configs(cfg);
		configs.addChangeListener(this);

		var sender = ReporterRegistry.getSender(LOG_SENDER, configs);
		var asyncProps = new LogAsyncProps(this.config, null);
		this.asyncReporter = DefaultAsyncReporter.create(sender,asyncProps);
		this.asyncReporter.startFlushThread();
	}

	public void report(LogData log) {
		this.asyncReporter.report(log);
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		var changes = filterChanges(list);
		if (changes.isEmpty()) {
			return;
		}
		this.config.updateConfigs(changes);
		this.refresh(this.config.getConfigs());
	}

	private Map<String, String> filterChanges(List<ChangeItem> list) {
		return list.stream()
				.filter(it -> it.fullName().startsWith(LOGS)
						|| it.fullName().startsWith(OUTPUT_SERVER_V2))
				.collect(Collectors.toMap(ChangeItem::fullName, ChangeItem::newValue));
	}

	public synchronized void refresh(Map<String, String> cfg) {
		var name = cfg.get(LOG_ACCESS_SENDER_NAME);
		var sender = asyncReporter.getSender();
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

		var asyncProps = new LogAsyncProps(this.config, null);
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
