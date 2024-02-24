package com.mawen.agent.plugin.report;

import java.io.Closeable;
import java.util.Map;

import com.mawen.agent.plugin.api.config.Config;

/**
 * borrow from OpenZipkin's Sender.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Sender extends Closeable {

	String name();

	void init(Config config, String prefix);

	Call<Void> send(EncodedData encodedData);

	boolean isAvailable();

	void updateConfigs(Map<String, String> changes);
}
