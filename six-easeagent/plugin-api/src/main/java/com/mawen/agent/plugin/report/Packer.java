package com.mawen.agent.plugin.report;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Pack a list of encoded items into a message package.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Packer {

	/**
	 * The encoder name
	 */
	String name();

	/**
	 * Combines a list of encoded items into an encoded list.
	 * For example, in thrift, this would be length-prefixed, whereas in json, this would be comma-separated and enclosed by brackets.
	 *
	 * @param encodedDataItems encoded item
	 * @return encoded list
	 */
	EncodedData encodeList(List<EncodedData> encodedDataItems);

	/**
	 * Calculate the size of a message package combined by a list of item
	 *
	 * @param encodedDataItems encodes item
	 * @return size of a whole message package
	 */
	default int messageSizeInBytes(List<EncodedData> encodedDataItems) {
		return packageSizeInBytes(encodedDataItems.stream().map(EncodedData::size).collect(Collectors.toList()));
	}

	/**
	 * Calculate the increase size when append a new message.
	 *
	 * @param newMsgSize the size of encoded message to append
	 * @return the increase size of a whole message package
	 */
	int appendSizeInBytes(int newMsgSize);

	/**
	 * Calculate the whole message package size combined of items
	 *
	 * @param sizes the size list of encoded items
	 * @return the size of a whole message package
	 */
	int packageSizeInBytes(List<Integer> sizes);
}
