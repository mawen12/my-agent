package com.mawen.agent.metrics.config;

import java.util.concurrent.TimeUnit;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.plugin.api.config.Config;

import static com.mawen.agent.plugin.api.config.ConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricsCollectorConfig implements MetricsConfig {

	private volatile boolean globalEnabled;
	private volatile boolean enabled;
	private volatile int interval;
	private Runnable callback;

	public MetricsCollectorConfig(Config config, String type) {
		ConfigUtils.bindProp(Observability.METRICS_ENABLED, config, Config::getBoolean, v -> this.globalEnabled = v);
		ConfigUtils.bindProp(join(Observability.METRICS, Observability.KEY_COMM_ENABLED), config, Config::getBoolean, v -> this.enabled = v);
		ConfigUtils.bindProp(join(Observability.METRICS, Observability.KEY_COMM_INTERVAL), config, Config::getInt, v -> {
			this.interval = v;
			if (callback != null) {
				callback.run();
			}
		});
	}

	@Override
	public boolean isEnabled() {
		return globalEnabled && enabled;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public TimeUnit getIntervalUnit() {
		return TimeUnit.SECONDS;
	}

	@Override
	public void setIntervalChangeCallback(Runnable runnable) {
		this.callback = runnable;
	}
}
