package com.mawen.agent.config.report;

import java.util.Map;

import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.Const;
import com.mawen.agent.plugin.utils.NoNull;
import com.mawen.agent.plugin.utils.common.StringUtils;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class ReportConfigAdapter {

	public static Map<String, String> extractReporterConfig(Config configs) {
		var config = ConfigUtils.extractByPrefix(configs.getConfigs(), REPORT);

		// default config
		config.put(TRACE_SENDER, NoNull.of(config.get(TRACE_ENCODER), SPAN_JSON_ENCODER_NAME));
		config.put(METRIC_ENCODER, NoNull.of(config.get(METRIC_ENCODER), METRIC_JSON_ENCODER_NAME));
		config.put(LOG_ENCODER, NoNull.of(config.get(LOG_ENCODER), LOG_DATA_JSON_ENCODER_NAME));
		config.put(LOG_ACCESS_ENCODER, NoNull.of(config.get(LOG_ACCESS_ENCODER), ACCESS_LOG_JSON_ENCODER_NAME));

		config.put(TRACE_SENDER_NAME, NoNull.of(config.get(TRACE_SENDER_NAME), getDefaultAppender(config)));
		config.put(METRIC_SENDER_NAME, NoNull.of(config.get(METRIC_SENDER_NAME), getDefaultAppender(config)));
		config.put(LOG_SENDER_NAME, NoNull.of(config.get(LOG_SENDER_NAME), getDefaultAppender(config)));
		config.put(LOG_ACCESS_SENDER_NAME, NoNull.of(config.get(LOG_ACCESS_SENDER_NAME), getDefaultAppender(config)));

		return config;
	}

	public static String getDefaultAppender(Map<String, String> cfg) {
		var outputAppender = cfg.get(join(OUTPUT_SERVER_V2, APPEND_TYPE_KEY));

		if (StringUtils.isEmpty(outputAppender)) {
			return Const.DEFAULT_APPEND_TYPE;
		}

		return outputAppender;
	}

	private ReportConfigAdapter() {
	}
}
