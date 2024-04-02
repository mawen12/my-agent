package com.mawen.agent.mock.context;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.ProgressFields;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.Getter;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Setter;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.Tracing;
import com.mawen.agent.plugin.bridge.NoOpCleaner;
import com.mawen.agent.plugin.bridge.NoOpPluginConfig;
import com.mawen.agent.plugin.bridge.NoOpTracer;
import com.mawen.agent.plugin.field.NullObject;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class SessionContext implements InitializeContext {
	private static final Logger log = LoggerFactory.getLogger(SessionContext.class.getName());

	private ITracing tracing = NoOpTracer.NO_OP_TRACING;
	private Supplier<InitializeContext> supplier;
	private final Deque<IPluginConfig> configs = new ArrayDeque<>();
	private final Deque<Object> retStack = new ArrayDeque<>();
	private final Deque<RetBound> retBound = new ArrayDeque<>();
	private final Map<Object, Object> context = new HashMap<>();
	private final Map<Object, Integer> entered = new HashMap<>();
	private boolean hasCleaner = false;

	public ITracing getTracing() {
		return tracing;
	}

	public void setTracing(ITracing tracing) {
		this.tracing = tracing;
	}

	public void setSupplier(Supplier<InitializeContext> supplier) {
		this.supplier = supplier;
	}

	public Supplier<InitializeContext> getSupplier() {
		return supplier;
	}

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public Tracing currentTracing() {
		return NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
	}

	@Override
	public <V> V get(Object key) {
		return change(context.get(key));
	}

	@Override
	public <V> V remove(Object key) {
		return change(context.remove(key));
	}

	@Override
	public <V> V put(Object key, V value) {
		context.put(key, value);
		return value;
	}

	@Override
	public IPluginConfig getConfig() {
		if (configs.isEmpty()) {
			log.warn("context.configs was empty.");
			return NoOpPluginConfig.INSTANCE;
		}
		return configs.peek();
	}

	@Override
	public int enter(Object key) {
		var count = entered.get(key);
		if (count == null) {
			count = 1;
		} else {
			count++;
		}
		entered.put(key, count);
		return count;
	}

	@Override
	public int exit(Object key) {
		var count = entered.get(key);
		if (count == null) {
			return 0;
		}
		entered.put(key, count - 1);
		return count;
	}

	@Override
	public AsyncContext exportAsync() {
		return AsyncContextImpl.build(tracing.exportAsync(), supplier, context);
	}

	@Override
	public Cleaner importAsync(AsyncContext snapshot) {
		var scope = tracing.importAsync(snapshot.getSpanContext());
		context.putAll(snapshot.getAll());
		if (hasCleaner) {
			return new AsyncCleaner(scope, false);
		} else {
			hasCleaner = true;
			return new AsyncCleaner(scope, true);
		}
	}

	@Override
	public Runnable wrap(Runnable task) {
		return new CurrentContextRunnable(exportAsync(), task);
	}

	@Override
	public boolean isWrapped(Runnable task) {
		return task instanceof CurrentContextRunnable;
	}

	@Override
	public RequestContext clientRequest(Request request) {
		return tracing.clientRequest(request);
	}

	@Override
	public RequestContext serverReceive(Request request) {
		return tracing.serverReceive(request);
	}

	@Override
	public Span consumerSpan(MessagingRequest request) {
		return tracing.consumerSpan(request);
	}

	@Override
	public Span producerSpan(MessagingRequest request) {
		return tracing.producerSpan(request);
	}

	@Override
	public void consumerInject(Span span, MessagingRequest request) {
		var injector = tracing.messagingTracing().consumerInjector();
		injector.inject(span, request);
	}

	@Override
	public void producerInject(Span span, MessagingRequest request) {
		var injector = tracing.messagingTracing().producerInjector();
		injector.inject(span, request);
	}

	@Override
	public Span nextSpan() {
		return tracing.nextSpan();
	}

	@Override
	public boolean isNecessaryKeys(String key) {
		return tracing.propagationKeys().contains(key);
	}

	@Override
	public void injectForwardedHeaders(Setter setter) {
		var fields = ProgressFields.getForwardHeaderSet();
		if (fields.isEmpty()) {
			return;
		}
		for (var field : fields) {
			var o = context.get(field);
			if (o instanceof String str) {
				setter.setHeader(field, str);
			}
		}
	}

	@Override
	public Cleaner importForwardHeaders(Getter getter) {
		return importForwardedHeaders(getter, Setter.NOOP_INSTANCE);
	}

	@Override
	public void pushConfig(IPluginConfig config) {
		configs.push(config);
	}

	@Override
	public IPluginConfig popConfig() {
		if (configs.isEmpty()) {
			log.warn("context.configs was empty.");
			return NoOpPluginConfig.INSTANCE;
		}
		return configs.pop();
	}

	@Override
	public <V> V putLocal(String key, V value) {
		assert this.retBound.peek() != null;
		this.retBound.peek().put(key, value);
		return value;
	}

	@Override
	public <V> V getLocal(String key) {
		assert this.retBound.peek() != null;
		return change(this.retBound.peek().get(key));
	}

	@Override
	public <T> void push(T obj) {
		if (obj == null) {
			this.retStack.push(NullObject.NULL);
		} else {
			this.retStack.push(obj);
		}
	}

	@Override
	public <T> T pop() {
		if (this.retStack.size() <= this.retBound.peek().size) {
			return null;
		}
		var o = this.retStack.pop();
		if (o == NullObject.NULL) {
			return null;
		}
		return change(o);
	}

	@Override
	public <T> T peek() {
		if (this.retStack.isEmpty()) {
			return null;
		}
		var o = this.retStack.pop();
		if (o == NullObject.NULL) {
			return null;
		}
		return change(o);
	}

	@Override
	public void pushRetBound() {
		this.retBound.push(new RetBound(this.retStack.size()));
	}

	@Override
	public void popRetBound() {
		this.retBound.pop();
	}

	@Override
	public void popToBound() {
		while (this.retStack.size() > this.retBound.peek().size) {
			this.retStack.pop();
		}
	}

	@Override
	public void clear() {
		if (!this.configs.isEmpty()) {
			this.configs.clear();
		}
		if (!this.retStack.isEmpty()) {
			this.retStack.clear();
		}
		if (!this.retBound.isEmpty()) {
			this.retBound.clear();
		}
		if (!this.context.isEmpty()) {
			this.context.clear();
		}
		if (!this.entered.isEmpty()) {
			this.entered.clear();
		}
		this.hasCleaner = false;
	}

	@Override
	public void setCurrentTracing(ITracing tracing) {
		this.tracing = NoNull.of(tracing, NoOpTracer.NO_OP_TRACING);
	}

	private <V> V change(Object o) {
		return o == null ? null : (V) o;
	}

	private Cleaner importForwardedHeaders(Getter getter, Setter setter) {
		var fields = ProgressFields.getForwardHeaderSet();
		if (fields.isEmpty()) {
			return NoOpCleaner.INSTANCE;
		}

		var fieldArr = new ArrayList<String>(fields.size());
		for (var field : fields) {
			var o = getter.header(field);
			if (o == null) {
				continue;
			}
			fieldArr.add(field);
			this.context.put(field, o);
			setter.setHeader(field, o);
		}
		if (fieldArr.isEmpty()) {
			return NoOpCleaner.INSTANCE;
		}
		return new FieldCleaner(fieldArr);
	}

	public class AsyncCleaner implements Cleaner {

		private final Scope scope;
		private final boolean clearContext;

		public AsyncCleaner(Scope scope, boolean clearContext) {
			this.scope = scope;
			this.clearContext = clearContext;
		}

		@Override
		public void close() {
			this.scope.close();
			if (clearContext) {
				SessionContext.this.clear();
			}
		}
	}

	public static record CurrentContextRunnable(AsyncContext asyncContext, Runnable task) implements Runnable {
		@Override
		public void run() {
			try (var cleaner = asyncContext.importToCurrent()) {
				task.run();
			}
		}
	}

	private class FieldCleaner implements Cleaner {

		private final List<String> fields;

		public FieldCleaner(List<String> fields) {
			this.fields = fields;
		}

		@Override
		public void close() {
			for (String field : fields) {
				context.remove(field);
			}
		}
	}

}
