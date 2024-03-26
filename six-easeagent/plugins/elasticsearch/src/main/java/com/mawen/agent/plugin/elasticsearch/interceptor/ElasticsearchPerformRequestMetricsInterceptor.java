package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.context.ContextUtils;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
import com.mawen.agent.plugin.elasticsearch.points.ElasticsearchPerformRequestPoints;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

import static com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AdviceTo(value = ElasticsearchPerformRequestPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestMetricsInterceptor extends ElasticsearchBaseMetricsInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		Request request = (Request) methodInfo.getArgs()[0];
		context.put(REQUEST, request);
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Response response = (Response) methodInfo.getRetValue();
		Request request = (Request) methodInfo.getArgs()[0];
		boolean success = checkSuccess(response, methodInfo.getThrowable());
		this.elasticsearchMetric.collectMetric(ElasticsearchCtxUtils.getIndex(request.getEndpoint()),
				ContextUtils.getDuration(context), success);
	}
}
