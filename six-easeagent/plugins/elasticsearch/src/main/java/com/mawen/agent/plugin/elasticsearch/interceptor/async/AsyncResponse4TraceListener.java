package com.mawen.agent.plugin.elasticsearch.interceptor.async;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class AsyncResponse4TraceListener implements ResponseListener {

	private final ResponseListener delegate;
	private final AsyncContext asyncContext;

	public AsyncResponse4TraceListener(ResponseListener delegate, AsyncContext asyncContext) {
		this.delegate = delegate;
		this.asyncContext = asyncContext;
	}

	public ResponseListener delegate() {
		return delegate;
	}

	public AsyncContext asyncContext() {
		return asyncContext;
	}

	@Override
	public void onSuccess(Response response) {
		try {
			this.delegate.onSuccess(response);
		}
		finally {
			this.process(response, null);
		}
	}

	@Override
	public void onFailure(Exception e) {
		try {
			this.delegate.onFailure(e);
		}
		finally {
			this.process(null, e);
		}
	}

	private void process(Response response, Exception exception) {
		try (Cleaner ignored = asyncContext.importToCurrent()) {
			Context context = Agent.getContext();
			ElasticsearchCtxUtils.finishSpan(response, exception, context);
		}
	}
}
