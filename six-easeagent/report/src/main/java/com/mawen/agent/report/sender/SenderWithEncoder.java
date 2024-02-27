package com.mawen.agent.report.sender;

import java.util.List;

import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;
import com.mawen.agent.plugin.report.Sender;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/27
 */
public interface SenderWithEncoder extends Sender {

	<T> Encoder<T> getEncoder();

	/**
	 * Sends a list of encoded data to a transport such as http or Kafka.
	 *
	 * @param encodedData list of encoded data, such as encoded spans.
	 * @throws IllegalStateException if {@link #close()} was called.
	 */
	Call<Void> send(List<EncodedData> encodedData);

	/**
	 * return sender prefix, eg.tracing sender: reporter.tracing.sender
	 *
	 * @return sender prefix
	 */
	String getPrefix();
}
