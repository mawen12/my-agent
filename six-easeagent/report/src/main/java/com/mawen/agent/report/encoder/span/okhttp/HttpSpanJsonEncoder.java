package com.mawen.agent.report.encoder.span.okhttp;

import java.util.List;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.encoder.span.SpanJsonEncoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@AutoService(Encoder.class)
public class HttpSpanJsonEncoder implements Encoder<ReportSpan>{
	public static final String ENCODER_NAME = ReportConfigConst.HTTP_SPAN_JSON_ENCODER_NAME;

	SpanJsonEncoder encoder;

	@Override
	public void init(Config config) {
		this.encoder = new SpanJsonEncoder();
		this.encoder.init(config);
	}

	@Override
	public int sizeInBytes(ReportSpan input) {
		return this.encoder.sizeInBytes(input);
	}

	@Override
	public EncodedData encode(ReportSpan input) {
		return new OkHttp;
	}

	@Override
	public String name() {
		return "";
	}

	@Override
	public EncodedData encodeList(List<EncodedData> encodedDataItems) {
		return null;
	}

	@Override
	public int appendSizeInBytes(int newMsgSize) {
		return 0;
	}

	@Override
	public int packageSizeInBytes(List<Integer> sizes) {
		return 0;
	}
}
