package com.mawen.agent.plugin.bridge;

import java.util.Collections;
import java.util.Iterator;
import java.util.Map;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.Getter;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Response;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Setter;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.SpanContext;
import com.mawen.agent.plugin.api.trace.Tracing;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class NoOpContext {
	public static final NoopContext NO_OP_CONTEXT = NoopContext.INSTANCE;
	public static final EmptyAsyncContext NO_OP_ASYNC_CONTEXT = EmptyAsyncContext.INSTANCE;
	public static final NoopRequestContext NO_OP_PROGRESS_CONTEXT = NoopRequestContext.INSTANCE;

	public enum NoopContext implements InitializeContext {
		INSTANCE;
		private static final Iterator<String> EMPTY_KEYS = new Iterator<String>() {
			@Override
			public boolean hasNext() {
				return false;
			}

			@Override
			public String next() {
				return null;
			}
		};

		@Override
		public void pushConfig(IPluginConfig config) {
			// NOP
		}

		@Override
		public IPluginConfig popConfig() {
			return NoOpPluginConfig.INSTANCE;
		}

		@Override
		public <V> V putLocal(String key, V value) {
			return null;
		}

		@Override
		public <V> V getLocal(String key) {
			return null;
		}

		@Override
		public <T> void push(T obj) {
			// NOP
		}

		@Override
		public <T> T pop() {
			return null;
		}

		@Override
		public <T> T peek() {
			return null;
		}

		@Override
		public void pushRetBound() {
			// NOP
		}

		@Override
		public void popRetBound() {
			// NOP
		}

		@Override
		public void popToBound() {
			// NOP
		}

		@Override
		public void clear() {
			// NOP
		}

		@Override
		public void setCurrentTracing(ITracing tracing) {
			// NOP
		}

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public Tracing currentTracing() {
			return NoOpTracer.NO_OP_TRACING;
		}

		@Override
		public <V> V get(Object key) {
			return null;
		}

		@Override
		public <V> V remove(Object key) {
			return null;
		}

		@Override
		public <V> V put(Object key, V value) {
			return value;
		}

		@Override
		public IPluginConfig getConfig() {
			return NoOpPluginConfig.INSTANCE;
		}

		@Override
		public int enter(Object key) {
			return 0;
		}

		@Override
		public int exit(Object key) {
			return 0;
		}

		@Override
		public AsyncContext exportAsync() {
			return EmptyAsyncContext.INSTANCE;
		}

		@Override
		public Cleaner importAsync(AsyncContext snapshot) {
			return NoOpCleaner.INSTANCE;
		}

		@Override
		public Runnable wrap(Runnable task) {
			return task;
		}

		@Override
		public boolean isWrapped(Runnable task) {
			return true;
		}

		@Override
		public RequestContext clientRequest(Request request) {
			return NoopRequestContext.INSTANCE;
		}

		@Override
		public RequestContext serverReceive(Request request) {
			return NoopRequestContext.INSTANCE;
		}

		@Override
		public Span consumerSpan(MessagingRequest request) {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public Span producerSpan(MessagingRequest request) {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public void consumerInject(Span span, MessagingRequest request) {
			// NOP
		}

		@Override
		public void producerInject(Span span, MessagingRequest request) {
			// NOP
		}

		@Override
		public Span nextSpan() {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public boolean isNecessaryKeys(String key) {
			return false;
		}

		@Override
		public void injectForwardedHeaders(Setter setter) {
			// NOP
		}

		@Override
		public Cleaner importForwardHeaders(Getter getter) {
			return NoOpCleaner.INSTANCE;
		}
	}

	public enum EmptyAsyncContext implements AsyncContext {
		INSTANCE;

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public SpanContext getSpanContext() {
			return NoOpTracer.NO_OP_SPAN_CONTEXT;
		}

		@Override
		public Cleaner importToCurrent() {
			return NoOpCleaner.INSTANCE;
		}

		@Override
		public Map<Object, Object> getAll() {
			return Collections.emptyMap();
		}

		@Override
		public <T> T get(Object key) {
			return null;
		}

		@Override
		public <V> V put(Object key, V value) {
			return null;
		}
	}

	public enum NoopRequestContext implements RequestContext {
		INSTANCE;

		@Override
		public boolean isNoop() {
			return true;
		}

		@Override
		public Span span() {
			return NoOpTracer.NO_OP_SPAN;
		}

		@Override
		public Scope scope() {
			return NoOpTracer.NO_OP_SCOPE;
		}

		@Override
		public void setHeader(String name, String value) {
			// NOP
		}

		@Override
		public Map<String, String> getHeaders() {
			return Collections.emptyMap();
		}

		@Override
		public void finish(Response response) {
			// NOP
		}
	}
}
