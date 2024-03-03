package com.mawen.agent.report.sender;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.Callback;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/3
 */
@AutoService(Sender.class)
public class AgentLoggerSender implements Sender{
	public static final String SENDER_NAME = ReportConfigConst.CONSOLE_SENDER_NAME;
	private static final Logger LOGGER = LoggerFactory.getLogger(AgentLoggerSender.class);
	private String prefix;

	@Override
	public String name() {
		return SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		// ignored
		this.prefix = prefix;
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		return new ConsoleCall(encodedData.getData());
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

	static class ConsoleCall implements Call<Void> {
		private final byte[] msg;

		ConsoleCall(byte[] msg) {
			this.msg = msg;
		}

		@Override
		public Void execute() throws IOException {
			LOGGER.info("{}",new String(msg));
			return null;
		}

		@Override
		public void enqueue(Callback<Void> cb) {
			LOGGER.debug("{}",new String(msg));
		}
	}
}
