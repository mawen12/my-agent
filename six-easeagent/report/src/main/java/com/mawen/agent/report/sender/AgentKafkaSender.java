package com.mawen.agent.report.sender;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.ConfigUtils;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.plugin.NoOpCall;
import zipkin2.codec.Encoding;
import zipkin2.reporter.kafka11.KafkaSender;
import zipkin2.reporter.kafka11.SDKKafkaSender;

import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
@AutoService(Sender.class)
public class AgentKafkaSender implements Sender {
	private static final String SENDER_NAME = KAFKA_SENDER_NAME;

	private boolean enabled;
	private Config config;

	SDKKafkaSender sender;
	Map<String, String> ssl;
	String prefix;
	String topicKey;
	String topic;
	String maxByteKey;

	@Override
	public String name() {
		return SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		this.config = config;
		this.prefix = prefix;
		String outputServer = config.getString(BOOTSTRAP_SERVERS);
		if (StringUtils.isEmpty(outputServer)) {
			this.enabled = false;
			return;
		}
		else {
			enabled = checkEnable(config);
		}
		this.topicKey = join(this.prefix, TOPIC_KEY);
		this.topic = config.getString(topicKey);

		this.maxByteKey = StringUtils.replaceSuffix(this.prefix, join(ASYNC_KEY, ASYNC_MSG_MAX_BYTES_KEY));
		int msgMaxBytes = config.getInt(maxByteKey);
		this.ssl = ConfigUtils.extractByPrefix(config, OUTPUT_SERVERS_SSL);

		this.sender = SDKKafkaSender.wrap(KafkaSender.newBuilder()
				.bootstrapServers(outputServer)
				.topic(this.topic)
				.overrides(ssl)
				.encoding(Encoding.JSON)
				.messageMaxBytes(msgMaxBytes)
				.build());
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		if (!enabled) {
			return new NoOpCall<>();
		}
		zipkin2.Call<Void> call = this.sender.sendSpans(encodedData.getData());
		return new ZipkinCallWrapper<>(call);
	}

	@Override
	public boolean isAvailable() {
		return (this.sender != null) && (!this.sender.isClose());
	}

	@Override
	public void close() throws IOException {
		if (this.sender != null) {
			this.sender.close();
		}
	}

	private boolean checkEnable(Config config) {
		return config.getBoolean(join(this.prefix, ENABLED_KEY), true) ? config.getBoolean(OUTPUT_SERVERS_ENABLE) : false;
	}
}
