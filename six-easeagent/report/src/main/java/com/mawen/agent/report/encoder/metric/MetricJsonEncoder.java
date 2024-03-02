package com.mawen.agent.report.encoder.metric;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.encoder.JsonEncoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@AutoService(Encoder.class)
public class MetricJsonEncoder extends JsonEncoder<Map<String, Object>> {
	public static final String ENCODER_NAME = ReportConfigConst.METRIC_JSON_ENCODER_NAME;

	static final String ENCODER_TMP = "__agent_encoded__";
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public String name() {
		return ENCODER_NAME;
	}

	@Override
	public void init(Config config) {
		// ignored
	}

	@Override
	public int sizeInBytes(Map<String, Object> input) {
		EncodedData d;
		if (input.get(ENCODER_TMP) != null) {
			d = (EncodedData) input.get(ENCODER_TMP);
		} else {
			d = encode(input);
			input.put(ENCODER_TMP, d);
		}
		return d.size();
	}

	@Override
	public EncodedData encode(Map<String, Object> input) {
		try {
			byte[] data;
			if (input.get(ENCODER_TMP) != null) {
				data = (byte[]) input.get(ENCODER_TMP);
			} else {
				data = this.objectMapper.writeValueAsBytes(input);
				input.put(ENCODER_TMP, data);
			}
			return new ByteWrapper(data);
		} catch (JsonProcessingException e) {
			// ignored
		}
		return new ByteWrapper(new byte[0]);
	}
}
