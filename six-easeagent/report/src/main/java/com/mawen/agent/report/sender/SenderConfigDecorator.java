package com.mawen.agent.report.sender;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mawen.agent.config.Configs;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.plugin.ReporterRegistry;

import static com.mawen.agent.config.ConfigUtils.*;
import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class SenderConfigDecorator implements SenderWithEncoder {
	private static final Logger log = LoggerFactory.getLogger(SenderConfigDecorator.class);

	private final Sender sender;
	private final String prefix;
	private final Config senderConfig;
	private final Config packerConfig;
	private final String encoderKey;
	private Encoder<?> packer;

	public SenderConfigDecorator(String prefix, Sender sender, Config config) {
		this.sender = sender;
		this.prefix = prefix;
		this.encoderKey = getEncoderKey(prefix);

		this.senderConfig = new Configs(extractSenderConfig(this.prefix, config));
		this.packerConfig = new Configs(extractSenderConfig(encoderKey, config));
	}

	@Override
	public void init(Config config, String prefix) {
		this.packer = ReporterRegistry.getEncoder(config.getString(this.encoderKey));
		this.packer.init(this.packerConfig);
		this.sender.init(this.senderConfig, prefix);
	}

	@Override
	public Call<Void> send(List<EncodedData> encodedData) {
		EncodedData data = this.packer.encodeList(encodedData);
		log.debugIfEnabled(new String(data.getData()));
		return send(data);
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		return this.sender.send(encodedData);
	}

	@Override
	public boolean isAvailable() {
		return sender.isAvailable();
	}

	@Override
	public void close() throws IOException {
		sender.close();
	}

	@Override
	public <T> Encoder<T> getEncoder() {
		return (Encoder<T>) this.packer;
	}

	@Override
	public String getPrefix() {
		return this.prefix;
	}

	@Override
	public String name() {
		return sender.name();
	}

	private static String getEncoderKey(String prefix) {
		var idx = prefix.lastIndexOf(".");
		if (idx == 0) {
			return ENCODER_KEY;
		} else {
			return prefix.substring(0, idx + 1) + ENCODER_KEY;
		}
	}

	private static Map<String, String> extractSenderConfig(String cfgPrefix, Config config) {
		var extract = extractByPrefix(config, cfgPrefix);
		var cfg = new HashMap<>(extract);

		// outputServer config
		cfg.putAll(extractByPrefix(config, REPORT));

		return cfg;
	}
}
