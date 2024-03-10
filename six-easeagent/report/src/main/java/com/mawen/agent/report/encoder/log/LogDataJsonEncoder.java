package com.mawen.agent.report.encoder.log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.Configs;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;
import com.mawen.agent.plugin.api.otlp.common.AgentLogData;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.encoder.JsonEncoder;
import zipkin2.internal.JsonCodec;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/28
 */
@AutoService(Encoder.class)
public class LogDataJsonEncoder extends JsonEncoder<AgentLogData> implements ConfigChangeListener {

	public static final String ENCODER_NAME = ReportConfigConst.LOG_DATA_JSON_ENCODER_NAME;

	Config encoderConfig;
	LogDataWriter writer;

	@Override
	public void init(Config config) {
		config.addChangeListener(this);
		this.encoderConfig = new Configs(getEncoderConfig(config.getConfigs()));
		this.writer = new LogDataWriter(this.encoderConfig);
	}

	@Override
	public String name() {
		return ENCODER_NAME;
	}

	@Override
	public int sizeInBytes(AgentLogData input) {
		return this.writer.sizeInBytes(input);
	}

	@Override
	public void onChange(List<ChangeItem> list) {
		if (list.isEmpty()) {
			return;
		}
		var changes = new HashMap<String, String>();
		list.forEach(change -> changes.put(change.fullName(), change.newValue()));
		Map<String, String> encoderChanges = getEncoderConfig(changes);
		if (encoderChanges.isEmpty()) {
			return;
		}
		var cfg = this.encoderConfig.getConfigs();
		cfg.putAll(encoderChanges);
		this.encoderConfig = new Configs(cfg);
		this.writer = new LogDataWriter(this.encoderConfig);
	}

	@Override
	public EncodedData encode(AgentLogData input) {
		try {
			var d = input.getEncodedData();
			if (d == null) {
				d = new ByteWrapper(JsonCodec.write(writer, input));
				input.setEncodedData(d);
			}
			return d;
		}
		catch (Exception e) {
			return new ByteWrapper(new byte[0]);
		}
	}

	private Map<String, String> getEncoderConfig(Map<String, String> cfgMap) {
		var encoderMap = new TreeMap<String, String>();

		cfgMap.forEach((k, v) -> {
			if (k.contains(ReportConfigConst.ENCODER_KEY) && !k.endsWith(ReportConfigConst.ENCODER_KEY)) {
				encoderMap.put(k.substring(k.lastIndexOf('.') + 1), v);
			}
		});

		return encoderMap;
	}
}
