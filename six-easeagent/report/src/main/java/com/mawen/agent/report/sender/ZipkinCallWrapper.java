package com.mawen.agent.report.sender;

import java.io.IOException;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.Callback;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ZipkinCallWrapper<V> implements Call<V> {

	private static final Logger log = LoggerFactory.getLogger(ZipkinCallWrapper.class);

	private final zipkin2.Call<V> call;

	public ZipkinCallWrapper(zipkin2.Call<V> call) {
		this.call = call;
	}

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

	static class ZipkinCallbackWrapper<V> implements zipkin2.Callback<V> {

		final Callback<V> delegate;

		public ZipkinCallbackWrapper(Callback<V> delegate) {
			this.delegate = delegate;
		}

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
