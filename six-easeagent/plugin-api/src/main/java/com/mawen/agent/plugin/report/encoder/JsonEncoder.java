package com.mawen.agent.plugin.report.encoder;


import java.util.List;

import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;

/**
 * JSON Encoder
 *
 * @param <T> abstract type
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public abstract class JsonEncoder<T> implements Encoder<T> {

	@Override
	public EncodedData encodeList(List<EncodedData> encodedDataItems) {
		int sizeOfArray = 2;
		int length = encodedDataItems.size();
		for (int i = 0; i < length; i++) {
			sizeOfArray += encodedDataItems.get(i++).size();
			if (i < length) sizeOfArray++;
		}

		byte[] buf = new byte[sizeOfArray];
		int pos = 0;
		buf[pos++] = '[';
		for (int i = 0; i <length;) {
			byte[] v = encodedDataItems.get(i++).getData();
			System.arraycopy(v, 0, buf, pos, v.length);
			pos += v.length;
			if (i < length) buf[pos++] = ',';
		}
		buf[pos] = ']';
		return new ByteWrapper(buf);
	}

	@Override
	public int packageSizeInBytes(List<Integer> sizes) {
		int sizeInBytes = 2;

		if (sizes != null && !sizes.isEmpty()) {
			for (Integer size : sizes) {
				sizeInBytes += size;
				sizeInBytes++;
			}
			sizeInBytes--;
		}

		return sizeInBytes;
	}

	@Override
	public int appendSizeInBytes(int newMsgSize) {
		return newMsgSize + 1;
	}
}
