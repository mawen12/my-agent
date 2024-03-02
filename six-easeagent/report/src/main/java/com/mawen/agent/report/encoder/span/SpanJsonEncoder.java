package com.mawen.agent.report.encoder.span;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.encoder.JsonEncoder;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.report.GlobalExtractor;
import zipkin2.internal.JsonCodec;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@AutoService(Encoder.class)
public class SpanJsonEncoder extends JsonEncoder<ReportSpan> {

	public static final String ENCODER_NAME = ReportConfigConst.SPAN_JSON_ENCODER_NAME;

	AgentV2SpanWriter writer;

	@Override
	public void init(Config config) {
		GlobalExtractor extrasSupplier = GlobalExtractor.getInstance(Agent.getConfig());
		writer = new AgentV2SpanWriter(extrasSupplier);
	}

	@Override
	public String name() {
		return ENCODER_NAME;
	}

	@Override
	public int sizeInBytes(ReportSpan input) {
		return writer.sizeInBytes(input);
	}

	@Override
	public EncodedData encode(ReportSpan span) {
		return new ByteWrapper(JsonCodec.write(writer, span));
	}
}
