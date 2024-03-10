package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.context.ContextUtils;
import com.mawen.agent.plugin.bridge.Agent;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseListener;

import static com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public record AsyncResponse4MetricsListener(
		ResponseListener delegate,
		AsyncContext asyncContext,
		ElasticsearchMetric elasticsearchMetric
) implements ResponseListener {

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
		catch (Exception ex) {
			this.process(null, e);
		}
	}

	private void process(Response response, Exception ex) {
		try (Cleaner ignored = asyncContext.importToCurrent()) {
			Context context = Agent.getContext();
			Request request = context.get(REQUEST);
			long duration = ContextUtils.getDuration(context);
			boolean success = checkSuccess(response, ex);
			this.elasticsearchMetric.collectMetric(ElasticsearchCtxUtils.getIndex(request.getEndpoint()), duration, success);
		}
	}
}
