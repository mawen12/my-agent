package com.mawen.agent.report.metric;

import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigAdapter;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.Const;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.plugin.utils.common.StringUtils;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public interface MetricProps {

	String getName();

	String getSenderPrefix();

	String getSenderName();

	String getTopic();

	int getInterval();

	boolean isEnabled();

	Configs asReportConfig();

	static MetricProps newDefault(IPluginConfig config,  Config reportConfig) {
		return new Default(reportConfig, config);
	}

	class Default implements MetricProps {

		private volatile String senderName;
		private final boolean enabled;

		// for kafka sender
		private final String topic;
		private final String name;

		private int interval;
		private final Config config;
		private final String senderPrefix;
		private final String asyncPrefix;
		private final Map<String,String> pluginConfigMap;

		public Default(Config reportConfig, IPluginConfig pluginConfig) {
			this.config = reportConfig;
			this.name = pluginConfig.namespace();
			this.senderPrefix = generatePrefix();
			this.asyncPrefix = getAsyncPrefix(this.senderPrefix);

			this.enabled = reportConfig.getBoolean(OUTPUT_SERVERS_ENABLE)
					&& reportConfig.getBoolean(METRIC_SENDER_ENABLED)
					&& reportConfig.getBoolean(join(this.senderPrefix, ENABLED_KEY));

			// low priority: global level
			Map<String, String> pCfg = ConfigUtils.extractByPrefix(reportConfig, REPORT);
			pCfg.putAll(ConfigUtils.extractAndConvertPrefix(pCfg, METRIC_SENDER, senderPrefix));
			pCfg.putAll(ConfigUtils.extractAndConvertPrefix(pCfg, METRIC_ENCODER, getEncoderKey(senderPrefix)));

			this.senderName = NoNull.of(pCfg.get(join(senderPrefix, APPEND_TYPE_KEY)),
					ReportConfigAdapter.getDefaultAppender(reportConfig.getConfigs()));

			this.topic = NoNull.of(pCfg.get(join(senderPrefix, TOPIC_KEY)), Const.METRIC_DEFAULT_TOPIC);

			if (pCfg.get(join(asyncPrefix, INTERVAL_KEY)) != null) {
				try {
					this.interval = Integer.parseInt(pCfg.get(join(asyncPrefix, INTERVAL_KEY)));
				}
				catch (NumberFormatException e) {
					this.interval = Const.METRIC_DEFAULT_INTERVAL;
				}
			} else {
				this.interval = Const.METRIC_DEFAULT_INTERVAL;
			}

			checkSenderName();

		}


		private String generatePrefix() {
			return "reporter.metric." + this.name + ".sender";
		}

		private static String getEncoderKey(String cfgPrefix) {
			return StringUtils.replaceSuffix(cfgPrefix, ENCODER_KEY);
		}

		private static String getAsyncPrefix(String cfgPrefix) {
			return StringUtils.replaceSuffix(cfgPrefix, ASYNC_KEY);
		}

		private void checkSenderName() {
			if ("kafka".equals(this.senderName)) {
				this.senderName = MetricKafkaSender.SENDER_NAME;
			}

			String bootstrapServers = this.config.getString(BOOTSTRAP_SERVERS);
			if (StringUtils.isEmpty(bootstrapServers) && this.senderName.equals(MetricKafkaSender.SENDER_NAME)) {
				this.senderName = AgentLoggerSender.SENDER_NAME;
			}
		}
	}
}
