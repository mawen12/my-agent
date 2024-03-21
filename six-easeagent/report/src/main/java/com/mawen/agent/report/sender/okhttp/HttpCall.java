package com.mawen.agent.report.sender.okhttp;

import java.io.IOException;

import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.Callback;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class HttpCall implements Call<Void>{

	final okhttp3.Call call;

	protected HttpCall(okhttp3.Call call) {
		this.call = call;
	}

	@Override
	public Void execute() throws IOException {
		try (var response = call.execute()) {
			parseResponse(response);
		}
		return null;
	}

	@Override
	public void enqueue(Callback<Void> delegate) {
		call.enqueue(new V2CallbackAdapter<>(delegate));
	}

	static void parseResponse(Response response) throws IOException {
		if (response.isSuccessful()) {
			return;
		}
		throw new IOException("response failed: " + response);
	}

	record V2CallbackAdapter<V>(Callback<V> delegate) implements okhttp3.Callback {
		@Override
		public void onFailure(@NotNull okhttp3.Call call, @NotNull IOException e) {
			delegate.onError(e);
		}

		@Override
		public void onResponse(@NotNull okhttp3.Call call, @NotNull Response response) throws IOException {
			try {
				parseResponse(response);
				delegate.onSuccess(null);
			}
			catch (IOException e) {
				delegate.onError(e);
			}
		}
	}
}
