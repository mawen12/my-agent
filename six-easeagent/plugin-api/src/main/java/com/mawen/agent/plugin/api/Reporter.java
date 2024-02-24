package com.mawen.agent.plugin.api;

import com.mawen.agent.plugin.report.EncodedData;

/**
 * A reported for message like metric or trace
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Reporter {

	/**
	 * out put the metric
	 *
	 * @param msg metric string like json
	 */
	void report(String msg);

	void report(EncodedData msg);
}
