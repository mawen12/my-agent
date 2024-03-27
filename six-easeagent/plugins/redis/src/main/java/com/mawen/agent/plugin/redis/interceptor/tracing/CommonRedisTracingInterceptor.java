package com.mawen.agent.plugin.redis.interceptor.tracing;

import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.middleware.MiddlewareConstants;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.api.middleware.Type;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
public abstract class CommonRedisTracingInterceptor implements NonReentrantInterceptor {
	private static final Object ENTER = new Object();
	private static final Object SPAN_KEY = new Object();

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
		Span currentSpan = context.currentTracing().currentSpan();
		if (currentSpan.isNoop()) {
			return;
		}
		doTraceBefore(methodInfo, context);
	}

	@Override
	public void doAfter(MethodInfo methodInfo, Context context) {
		try {
			Span span = context.get(SPAN_KEY);
			if (span == null) {
				return;
			}

			Throwable throwable = methodInfo.getThrowable();

			if (throwable != null) {
				span.error(throwable);
			}
			span.finish();
			context.remove(SPAN_KEY);
		}
		catch (Exception ignored) {}
	}

	@Override
	public int order() {
		return Order.TRACING.getOrder();
	}

	@Override
	public Object getEnterKey(MethodInfo methodInfo, Context context) {
		return ENTER;
	}

	protected abstract void doTraceBefore(MethodInfo methodInfo, Context context);

	protected void startTracing(Context context, String name, String uri, String cmd) {
		Span span = context.nextSpan().name(name).start();
		span.kind(Span.Kind.CLIENT);
		span.remoteServiceName("redis");
		context.put(SPAN_KEY, span);

		if (cmd != null) {
			span.tag("redis.method", cmd);
		}

		span.tag(MiddlewareConstants.TYPE_TAG_NAME, Type.REDIS.getRemoteType());
		RedirectProcessor.setTagsIfRedirected(Redirect.REDIS, span, null);
	}
}
