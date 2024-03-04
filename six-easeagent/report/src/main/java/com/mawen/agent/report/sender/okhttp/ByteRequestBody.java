package com.mawen.agent.report.sender.okhttp;

import java.io.IOException;

import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ByteRequestBody extends RequestBody {
	static final  MediaType CONTENT_TYPE = MediaType.parse("application/json");

	private final byte[] data;
	@Getter
	private final int contentLength;

	public ByteRequestBody(byte[] data) {
		this.data = data;
		this.contentLength = data.length;
	}

	@Nullable
	@Override
	public MediaType contentType() {
		return CONTENT_TYPE;
	}

	@Override
	public void writeTo(@NotNull BufferedSink sink) throws IOException {
		sink.write(data);
	}
}
