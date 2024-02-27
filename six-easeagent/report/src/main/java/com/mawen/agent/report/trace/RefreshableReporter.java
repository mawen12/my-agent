package com.mawen.agent.report.trace;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.report.async.AsyncProps;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public class RefreshableReporter<S> implements Reporter<S> {

	private SDKAsyncReporter<S> asyncReporter;
	private AsyncProps traceProperties;
	private Config reportConfig;

	@Override
	public void report(String msg) {

	}

	@Override
	public void report(EncodedData msg) {

	}
}
