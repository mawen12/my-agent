package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.report.EncodedData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public enum NoOpReporter implements Reporter {
	INSTANCE;

	@Override
	public void report(String msg) {
		// ignored
	}

	@Override
	public void report(EncodedData msg) {
		// ignored
	}
}
