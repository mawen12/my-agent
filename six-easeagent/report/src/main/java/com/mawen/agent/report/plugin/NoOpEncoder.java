package com.mawen.agent.report.plugin;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.ByteWrapper;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Encoder;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/29
 */
public class NoOpEncoder<S> implements Encoder<S> {

	public static final NoOpEncoder<?> INSTANCE = new NoOpEncoder<>();

	@Override
	public void init(Config config) {
		// ignored
	}

	@Override
	public int sizeInBytes(S input) {
		return input.toString().length();
	}

	@Override
	public EncodedData encode(S input) {
		return new ByteWrapper(input.toString().getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public String name() {
		return "noop";
	}

	@Override
	public EncodedData encodeList(List<EncodedData> encodedItems) {
		var sb = new StringBuilder();
		encodedItems.forEach(sb::append);
		return new ByteWrapper(sb.toString().getBytes(StandardCharsets.US_ASCII));
	}

	@Override
	public int appendSizeInBytes(int newMsgSize) {
		return newMsgSize;
	}

	@Override
	public int packageSizeInBytes(List<Integer> sizes) {
		return sizes.stream().mapToInt(s -> s).sum();
	}
}
