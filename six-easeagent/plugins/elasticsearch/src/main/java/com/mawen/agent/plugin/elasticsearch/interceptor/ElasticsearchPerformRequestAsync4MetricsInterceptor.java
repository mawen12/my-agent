package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
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
		var request = (Request) methodInfo.getArgs()[0];
		context.put(REQUEST, request);
		var asyncContext = context.exportAsync();
		var listener = (ResponseListener) methodInfo.getArgs()[1];
		var asyncResponse4MetricsListener = new AsyncResponse4MetricsListener(listener, asyncContext, elasticsearchMetric);
		methodInfo.changeArg(1, asyncResponse4MetricsListener);
	}
}
