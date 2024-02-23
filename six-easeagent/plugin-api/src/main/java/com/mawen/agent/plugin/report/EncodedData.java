package com.mawen.agent.plugin.report;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface EncodedData {
	int size();

	byte[] getData();

	EncodedData EMPTY = new EncodedData() {
		@Override
		public int size() {
			return 0;
		}

		@Override
		public byte[] getData() {
			return new byte[0];
		}
	};
}
