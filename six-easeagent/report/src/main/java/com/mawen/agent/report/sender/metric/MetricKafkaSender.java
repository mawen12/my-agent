package com.mawen.agent.report.sender.metric;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.OutputProperties;
import com.mawen.agent.report.metric.MetricProps;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@AutoService(Sender.class)
public class MetricKafkaSender implements Sender {

	public static final String SENDER_NAME = ReportConfigConst.METRIC_KAFKA_SENDER_NAME;

	private static volatile AppenderManager appenderManager;

	private OutputProperties outputProperties;
	private MetricProps props;
	private Logger logger;
	private String prefix;


	@Override
	public String name() {
		return SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		this.prefix = prefix;
		this.outputProperties = Utils
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		return null;
	}

	@Override
	public boolean isAvailable() {
		return false;
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {

	}

	@Override
	public void close() throws IOException {

	}
}
