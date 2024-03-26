package com.mawen.agent.plugin.elasticsearch.interceptor.async;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
import com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchBaseMetricsInterceptor;
import com.mawen.agent.plugin.elasticsearch.points.ElasticsearchPerformRequestAsyncPoints;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.ResponseListener;

import static com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AdviceTo(value = ElasticsearchPerformRequestAsyncPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestAsync4MetricsInterceptor extends ElasticsearchBaseMetricsInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		Request request = (Request) methodInfo.getArgs()[0];
		context.put(REQUEST, request);
		AsyncContext asyncContext = context.exportAsync();
		ResponseListener listener = (ResponseListener) methodInfo.getArgs()[1];
		AsyncResponse4MetricsListener asyncResponse4MetricsListener = new AsyncResponse4MetricsListener(listener, asyncContext, elasticsearchMetric);
		methodInfo.changeArg(1, asyncResponse4MetricsListener);
	}
}
