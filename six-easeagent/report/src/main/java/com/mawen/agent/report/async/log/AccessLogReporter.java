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
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.async.AsyncReporter;
import com.mawen.agent.report.async.DefaultAsyncReporter;
import com.mawen.agent.report.plugin.ReporterRegistry;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class AccessLogReporter implements ConfigChangeListener {

	Config config;
	AsyncReporter<AccessLogInfo> asyncReporter;

	public AccessLogReporter(Config configs) {
		var cfg = ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ACCESS);
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), OUTPUT_SERVER_V2));
		cfg.putAll(ConfigUtils.extractByPrefix(configs.getConfigs(), LOG_ASYNC));

		this.config = new Configs(cfg);
		configs.addChangeListener(this);

		var asyncProperties = new LogAsyncProps(this.config, LOG_ACCESS);
		var sender = ReporterRegistry.getSender(LOG_ACCESS_SENDER, this.config);
		this.asyncReporter = DefaultAsyncReporter.create(sender, asyncProperties);
		this.asyncReporter.startFlushThread();
	}

	public void report(AccessLogInfo log) {
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
				sender = ReporterRegistry.getSender(LOG_ACCESS_SENDER, config);
				asyncReporter.setSender(sender);
			}
		}
		else {
			sender = ReporterRegistry.getSender(LOG_ACCESS_SENDER, config);
			asyncReporter.setSender(sender);
		}

		var asyncProps = new LogAsyncProps(this.config, LOG_ACCESS);
		asyncReporter.closeFlushThread();
		asyncReporter.setPending(asyncProps.getQueuedMaxItems(), asyncProps.getQueuedMaxSize());
		asyncReporter.setMessageTimeoutNanos(messageTimeout(asyncProps.getMessageTimeout()));
		asyncReporter.startFlushThread();
	}

	private Map<String, String> filterChanges(List<ChangeItem> list) {
		return list.stream()
				.filter(it -> it.fullName().startsWith(LOG_ACCESS)
						|| it.fullName().startsWith(LOG_ASYNC)
						|| it.fullName().startsWith(OUTPUT_SERVER_V2))
				.collect(Collectors.toMap(ChangeItem::fullName, ChangeItem::newValue));
	}

	protected long messageTimeout(long timeout) {
		if (timeout < 0) {
			timeout = 1000L;
		}
		return TimeUnit.MILLISECONDS.toNanos(timeout);
	}
}
