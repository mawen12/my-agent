package com.mawen.agent.mock.context;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.trace.SpanContext;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public record AsyncContextImpl(SpanContext spanContext, Map<Object, Object> context, Supplier<InitializeContext> supplier) implements AsyncContext {

	public AsyncContextImpl(SpanContext spanContext, Map<Object, Object> context, Supplier<InitializeContext> supplier) {
		this.spanContext = Objects.requireNonNull(spanContext, "spanContext must not be null");
		this.context = Objects.requireNonNull(context, "context must not be null");
		this.supplier = Objects.requireNonNull(supplier, "supplier must not be null");
	}

	public static AsyncContextImpl build(SpanContext spanContext, Supplier<InitializeContext> supplier,
			Map<Object, Object> context) {
		Map<Object, Object> contextMap = context == null ? new HashMap<>() : new HashMap<>(context);
		return new AsyncContextImpl(spanContext, contextMap, supplier);
	}

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public SpanContext getSpanContext() {
		return spanContext;
	}

	@Override
	public Cleaner importToCurrent() {
		return supplier.get().importAsync(this);
	}

	@Override
	public Map<Object, Object> getAll() {
		return context;
	}

	@Override
	public <T> T get(Object key) {
		return (T) this.context.get(key);
	}

	@Override
	public <V> V put(Object key, V value) {
		return (V) this.context.put(key, value);
	}

}
