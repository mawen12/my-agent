package com.mawen.agent.zipkin;

import java.util.Map;
import java.util.function.Supplier;

import brave.handler.MutableSpan;
import brave.handler.SpanHandler;
import brave.propagation.TraceContext;
import com.mawen.agent.plugin.api.ProgressFields;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/20
 */
public class CustomTagsSpanHandler extends SpanHandler {

	private static final String TAG_INSTANCE = "";

	private final String instance;
	private final Supplier<String> serviceName;

	public CustomTagsSpanHandler(Supplier<String> serviceName, String instance) {
		this.instance = instance;
		this.serviceName = serviceName;
	}

	@Override
	public boolean end(TraceContext context, MutableSpan span, Cause cause) {
		span.tag(TAG_INSTANCE, this.instance);
		span.localServiceName(this.serviceName.get());
		fillTags(span, ProgressFields.getServiceTags());
		fillTags(span, RedirectProcessor.tags());
		return true;
	}

	protected void fillTags(MutableSpan span, Map<String, String> tags) {
		if (tags == null || tags.isEmpty()) {
			return;
		}
		for (Map.Entry<String, String> entry : tags.entrySet()) {
			span.tag(entry.getKey(), entry.getValue());
		}
	}
}
