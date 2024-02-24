package com.mawen.agent.plugin.report;

import com.mawen.agent.plugin.api.config.Config;

/**
 * borrow from zipkin's BytesEncoder Removing Encoding enum,
 * and add encoderName method, allow defining any kind of encoder with a unique name.
 * When the name is conflict with others, it will fail when loaded.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Encoder<T> extends Packer {

	/**
	 * encoder init method, called when loaded
	 *
	 * @param config report plugin configuration
	 */
	void init(Config config);

	/**
	 * The byte length of its encoded binary form
	 */
	int sizeInBytes(T input);

	/**
	 * Serializes an object into its binary form.
	 */
	EncodedData encode(T input);
}
