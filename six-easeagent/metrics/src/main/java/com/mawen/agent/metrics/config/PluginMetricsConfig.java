package com.mawen.agent.metrics.config;

import java.util.concurrent.TimeUnit;

import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.config.Const;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.config.PluginConfigChangeListener;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class PluginMetricsConfig implements MetricsConfig {

	private volatile boolean enabled;
	private volatile int interval;
	private volatile TimeUnit intervalUnit;
	private Runnable callback;

	public PluginMetricsConfig(IPluginConfig config) {
		set(config);
		config.addChangeListener(new PluginConfigChange());
	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public int getInterval() {
		return interval;
	}

	@Override
	public TimeUnit getIntervalUnit() {
		return intervalUnit;
	}

	@Override
	public void setIntervalChangeCallback(Runnable runnable) {
		this.callback = runnable;
	}

	private void set(IPluginConfig config) {
		this.enabled = config.enabled();
		this.interval = NoNull.of(config.getInt(ConfigConst.Observability.KEY_COMM_INTERVAL), Const.METRIC_DEFAULT_INTERVAL);
		String timeUnit = NoNull.of(config.getString(ConfigConst.Observability.KEY_COMM_INTERVAL_UNIT), Const.METRIC_DEFAULT_INTERVAL_UNIT);
		try {
			this.intervalUnit = TimeUnit.valueOf(timeUnit);
		}
		catch (IllegalArgumentException e) {
			this.intervalUnit = TimeUnit.SECONDS;
		}
	}

	class PluginConfigChange implements PluginConfigChangeListener {

		@Override
		public void onChange(IPluginConfig oldConfig, IPluginConfig newConfig) {
			var oldInterval = PluginMetricsConfig.this.interval;
			set(newConfig);
			var runnable = callback;
			if (oldInterval != PluginMetricsConfig.this.interval && runnable != null) {
				runnable.run();
			}
		}
	}
}
