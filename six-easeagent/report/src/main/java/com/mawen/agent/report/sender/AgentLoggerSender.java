package com.mawen.agent.report.sender;

import java.io.IOException;

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
public class AgentLoggerSender implements Sender {
	private static final Logger log = LoggerFactory.getLogger(AgentLoggerSender.class);

	private String prefix;

	@Override
	public void init(Config config, String prefix) {
		// ignored
		this.prefix = prefix;
	}

	@Override
	public String name() {
		return ReportConfigConst.CONSOLE_SENDER_NAME;
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
	public void close() throws IOException {
		// ignored
	}

	static class ConsoleCall implements Call<Void> {
		private final byte[] msg;

		public ConsoleCall(byte[] msg) {
			this.msg = msg;
		}

		public byte[] msg() {
			return msg;
		}

		@Override
		public Void execute() throws IOException {
			log.infoIfEnabled("{}", new String(msg));
			return null;
		}

		@Override
		public void enqueue(Callback<Void> cb) {
			log.debugIfEnabled("{}", new String(msg));
		}
	}
}
