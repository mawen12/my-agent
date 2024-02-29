package com.mawen.agent.report.sender;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.plugin.NoOpCall;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
@AutoService(Sender.class)
public class NoOpSender implements Sender{
	public static final NoOpSender INSTANCE = new NoOpSender();

	@Override
	public String name() {
		return ReportConfigConst.NOOP_SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		// ignored
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		return NoOpCall.getInstance(NoOpSender.class);
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {
		// ignored
	}

	@Override
	public void close() throws IOException {
		// ignored
	}
}
