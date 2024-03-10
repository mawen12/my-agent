package com.mawen.agent.report.sender;

import java.io.IOException;

import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.Callback;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@Slf4j
@AllArgsConstructor
public class ZipkinCallWrapper<V> implements Call<V> {

	private final zipkin2.Call<V> call;

	@Override
	public V execute() throws IOException {
		try {
			return call.execute();
		}
		catch (IOException e) {
			log.warn("Call exception: {}", e.getMessage());
			throw new IOException(e);
		}
	}

	@Override
	public void enqueue(Callback<V> cb) {
		var zCb = new ZipkinCallbackWrapper<>(cb);
		this.call.enqueue(zCb);
	}

	@AllArgsConstructor
	static class ZipkinCallbackWrapper<V> implements zipkin2.Callback<V> {

		final Callback<V> delegate;

		@Override
		public void onSuccess(V value) {
			this.delegate.onSuccess(value);
		}

		@Override
		public void onError(Throwable t) {
			this.delegate.onError(t);
		}
	}
}
