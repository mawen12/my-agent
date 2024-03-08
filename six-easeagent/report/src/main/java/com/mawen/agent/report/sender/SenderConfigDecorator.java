package com.mawen.agent.report.sender;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mawen.agent.config.Configs;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.plugin.ReporterRegistry;
import lombok.extern.slf4j.Slf4j;

import static com.mawen.agent.config.ConfigUtils.*;
import static com.mawen.agent.config.report.ReportConfigConst.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Slf4j
public class SenderConfigDecorator implements SenderWithEncoder, ConfigChangeListener {

	protected Sender sender;
	String prefix;
	Config senderConfig;
	Config packerConfig;
	String encoderKey;
	Encoder<?> packer;

	public SenderConfigDecorator(String prefix, Sender sender, Config config) {
		this.sender = sender;
		this.prefix = prefix;
		this.encoderKey = getEncoderKey(prefix);
		config.addChangeListener(this);
		this.senderConfig = new Configs(extractSenderConfig(this.prefix, config));
		this.packerConfig = new Configs(extractSenderConfig(encoderKey, config));
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		Map<String, String> changes = filterChanges(list);
		if (changes.isEmpty()) {
			return;
		}
		Map<String, String> senderChanges = new TreeMap<>();
		Map<String, String> packerChanges = new TreeMap<>();

		changes.forEach((key, value) -> {
			if (key.startsWith(encoderKey)) {
				packerChanges.put(key, value);
			} else {
				senderChanges.put(key, value);
			}
		});

		if (!packerChanges.isEmpty()) {
			this.packerConfig.updateConfigs(packerChanges);
			this.updateEncoder(packerChanges);
		}

		if (!senderChanges.isEmpty()) {
			this.senderConfig.updateConfigs(senderChanges);
			this.updateConfigs(senderChanges);
		}
	}

	@Override
	public <T> Encoder<T> getEncoder() {
		return (Encoder<T>) this.packer;
	}

	@Override
	public Call<Void> send(List<EncodedData> encodedData) {
		EncodedData data = this.packer.encodeList(encodedData);
		if (log.isDebugEnabled()) {
			log.debug(new String(data.getData()));
		}
		return send(encodedData);
	}

	@Override
	public String getPrefix() {
		return this.prefix;
	}

	@Override
	public String name() {
		return sender.name();
	}

	@Override
	public void init(Config config, String prefix) {
		this.packer = ReporterRegistry.getEncoder(config.getString(this.encoderKey));
		this.packer.init(this.packerConfig);
		this.sender.init(this.senderConfig, prefix);
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
	public void updateConfigs(Map<String, String> changes) {
		String name = changes.get(join(this.prefix, APPEND_TYPE_KEY));
		if (name == null || name.equals(name())) {
			this.sender.updateConfigs(changes);
		} else {
			try {
				this.sender.close();
			}
			catch (IOException e) {
				log.warn("Sender update fail, can not close sender: {}", this.sender.name());
			}
		}
	}

	@Override
	public void close() throws IOException {
		sender.close();
	}

	protected void updateEncoder(Map<String, String> changes) {
		String name = changes.get(this.encoderKey);
		if (name == null || name.equals(this.packer.name())) {
			return;
		}
		this.packer = ReporterRegistry.getEncoder(packerConfig.getString(this.encoderKey));
		this.packer.init(packerConfig);
	}

	private static String getEncoderKey(String prefix) {
		int idx = prefix.lastIndexOf(".");
		if (idx == 0) {
			return ENCODER_KEY;
		} else {
			return prefix.substring(0, idx + 1) + ENCODER_KEY;
		}
	}

	private static Map<String, String> extractSenderConfig(String cfgPrefix, Config config) {
		Map<String, String> extract = extractByPrefix(config, cfgPrefix);
		Map<String, String> cfg = new HashMap<>(extract);

		// outputServer config
		cfg.putAll(extractByPrefix(config, REPORT));

		return cfg;
	}

	private Map<String, String> filterChanges(List<ChangeItem> list) {
		Map<String, String> cfg = new HashMap<>();
		list.stream().filter(one -> {
			String name = one.getFullName();
			return name.startsWith(REPORT)
					|| name.startsWith(encoderKey)
					|| name.startsWith(prefix);
		}).forEach(one -> cfg.put(one.getFullName(), one.getNewValue()));

		return cfg;
	}
}
