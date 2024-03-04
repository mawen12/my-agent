package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
import com.mawen.agent.plugin.elasticsearch.points.ElasticsearchPerformRequestPoints;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.Response;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AdviceTo(value = ElasticsearchPerformRequestPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestTraceInterceptor extends ElasticsearchBaseTraceInterceptor{

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		ElasticsearchCtxUtils.initSpan(methodInfo, context);
	}

	@Override
	public void after(MethodInfo methodInfo, Context context) {
		Response response = (Response) methodInfo.getRetValue();
		ElasticsearchCtxUtils.finishSpan(response, methodInfo.getThrowable(), context);
	}
}
