package com.mawen.agent.plugin.elasticsearch.interceptor.async;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
import com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchBaseTraceInterceptor;
import com.mawen.agent.plugin.elasticsearch.interceptor.ElasticsearchCtxUtils;
import com.mawen.agent.plugin.elasticsearch.points.ElasticsearchPerformRequestAsyncPoints;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import org.elasticsearch.client.ResponseListener;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AdviceTo(value = ElasticsearchPerformRequestAsyncPoints.class, plugin = ElasticsearchPlugin.class)
public class ElasticsearchPerformRequestAsync4TraceInterceptor extends ElasticsearchBaseTraceInterceptor {

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		ElasticsearchCtxUtils.initSpan(methodInfo, context);
		var asyncContext = context.exportAsync();
		var listener = (ResponseListener) methodInfo.getArgs()[1];
		var asyncResponseListener = new AsyncResponse4TraceListener(listener, asyncContext);
		methodInfo.changeArg(1, asyncResponseListener);
	}
}
