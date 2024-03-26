package com.mawen.agent.plugin.elasticsearch.interceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.middleware.MiddlewareConstants;
import com.mawen.agent.plugin.api.middleware.Type;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.utils.common.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ElasticsearchCtxUtils {

	private static final String SPAN = ElasticsearchCtxUtils.class.getName() + "-Span";
	public static final String REQUEST = ElasticsearchCtxUtils.class.getName() + "-Request";


	public static void initSpan(MethodInfo methodInfo, Context context) {
		try {
			Request request = (Request) methodInfo.getArgs()[0];
			HttpEntity entity = request.getEntity();
			Span span = context.nextSpan();
			span.kind(Span.Kind.CLIENT);
			span.remoteServiceName("elasticsearch");
			span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.ELASTICSEARCH.getRemoteType());
			span.tag("es.index", getIndex(request.getEndpoint()));
			span.tag("es.operation", request.getMethod() + " " + request.getEndpoint());
			if (entity != null) {
				String body = EntityUtils.toString(entity, StandardCharsets.UTF_8);
				span.tag("es.body", body);
			}
			span.start();
			context.put(SPAN, span);
			context.put(REQUEST, request);
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String getIndex(String endpoint) {
		if (StringUtils.isEmpty(endpoint)) {
			return "";
		}
		String tmp = endpoint;
		if (!tmp.startsWith("/")) {
			tmp = "/" + tmp;
		}
		int end = tmp.indexOf("/", 1);
		String index;
		if (end < 0) {
			index = tmp.substring(1);
		}
		else if (end > 0) {
			index = tmp.substring(1, end);
		} else {
			index = tmp.substring(1);
		}
		if (index.startsWith("_") || index.startsWith("-") || index.startsWith("+")) {
			return "";
		}
		return index;
	}

	public static boolean checkSuccess(Response response, Throwable throwable) {
		if (throwable != null) {
			return false;
		}
		if (response == null) {
			return false;
		}
		return response.getStatusLine().getStatusCode() == 200
				|| response.getStatusLine().getStatusCode() == 201;
	}

	public static void finishSpan(Response response, Throwable throwable, Context context) {
		Span span = context.get(SPAN);
		if (span == null) {
			return;
		}
		if (throwable != null) {
			span.error(throwable);
			span.tag("error", throwable.getMessage());
		} else {
			if (!checkSuccess(response, null)) {
				if (response != null) {
					int statusCode = response.getStatusLine().getStatusCode();
					span.tag("error", String.valueOf(statusCode));
				} else {
					span.tag("error", "unknown");
				}
			}
		}
		span.finish();
		context.remove(SPAN);
	}
}
