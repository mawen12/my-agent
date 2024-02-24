package com.mawen.agent.plugin.bridge;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.report.EncodedData;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class NoOpReporter implements Reporter {
	public static final NoOpReporter NO_OP_REPORTER = new NoOpReporter();

	@Override
	public void report(String msg) {
		// ignored
	}

	@Override
	public void report(EncodedData msg) {
		// ignored
	}
}
