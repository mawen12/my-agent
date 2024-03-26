package com.mawen.agent.plugin.report;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ByteWrapper implements EncodedData{

	private final byte[] data;

	public ByteWrapper(byte[] data) {
		this.data = data;
	}

	@Override
	public int size() {
		return data.length;
	}

	@Override
	public byte[] getData() {
		return data;
	}
}
