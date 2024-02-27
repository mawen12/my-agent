package com.mawen.agent.report.encoder.log;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.logging.AccessLogInfo;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.encoder.JsonEncoder;
import zipkin2.internal.JsonCodec;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
@AutoService(Encoder.class)
public class AccessLogJsonEncoder extends JsonEncoder<AccessLogInfo> {

	public static final String ENCODER_NAME = ReportConfigConst.ACCESS_LOG_JSON_ENCODER_NAME;

	AccessLogWriter writer;

	@Override
	public void init(Config config) {
		this.writer = new AccessLogWriter();
	}

	@Override
	public int sizeInBytes(AccessLogInfo input) {
		if (input.getEncodedData() != null) {
			return input.getEncodedData().size();
		}
		return this.writer.sizeInBytes(input);
	}

	@Override
	public EncodedData encode(AccessLogInfo input) {
		try {
			EncodedData d = input.getEncodedData();
			if (d == null) {
				d = new ByteWrapper(JsonCodec.write(writer,input));
				input.setEncodedData(d);
			}
			return d;
		}
		catch (Exception e) {
			return new ByteWrapper(new byte[0]);
		}
	}

	@Override
	public String name() {
		return ENCODER_NAME;
	}
}
