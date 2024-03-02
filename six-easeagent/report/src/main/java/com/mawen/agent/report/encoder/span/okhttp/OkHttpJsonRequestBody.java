package com.mawen.agent.report.encoder.span.okhttp;

import java.io.IOException;

import com.mawen.agent.plugin.report.EncodedData;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class OkHttpJsonRequestBody extends RequestBody implements EncodedData {
	static final MediaType CONTENT_TYPE = MediaType.parse("application/json");

	private final byte[] data;
	@Getter
	private final int contentLength;

	public OkHttpJsonRequestBody(byte[] data) {
		this.data = data;
		this.contentLength = data.length;
	}

	@Override
	public int size() {
		return this.contentLength;
	}

	@Override
	public byte[] getData() {
		return new byte[0];
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
