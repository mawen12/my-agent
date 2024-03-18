package com.mawen.agent.core.instrument;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.context.AsyncContext;
import com.mawen.agent.plugin.api.context.RequestContext;
import com.mawen.agent.plugin.api.trace.Getter;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.MessagingRequest;
import com.mawen.agent.plugin.api.trace.Request;
import com.mawen.agent.plugin.api.trace.Setter;
import com.mawen.agent.plugin.api.trace.Span;
import com.mawen.agent.plugin.api.trace.Tracing;
import com.mawen.agent.plugin.bridge.NoOpContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class TestContext implements InitializeContext {

	private InitializeContext delegate = NoOpContext.NO_OP_CONTEXT;

	@Override
	public boolean isNoop() {
		return false;
	}

	@Override
	public Tracing currentTracing() {
		return delegate.currentTracing();
	}

	@Override
	public <V> V get(Object key) {
		return delegate.get(key);
	}

	@Override
	public <V> V remove(Object key) {
		return delegate.remove(key);
	}

	@Override
	public <V> V put(Object key, V value) {
		return delegate.put(key, value);
	}

	@Override
	public IPluginConfig getConfig() {
		return delegate.getConfig();
	}

	@Override
	public int enter(Object key) {
		return delegate.enter(key);
	}

	@Override
	public int exit(Object key) {
		return delegate.exit(key);
	}

	@Override
	public AsyncContext exportAsync() {
		return delegate.exportAsync();
	}

	@Override
	public Cleaner importAsync(AsyncContext snapshot) {
		return delegate.importAsync(snapshot);
	}

	@Override
	public Runnable wrap(Runnable task) {
		return delegate.wrap(task);
	}

	@Override
	public boolean isWrapped(Runnable task) {
		return delegate.isWrapped(task);
	}

	@Override
	public RequestContext clientRequest(Request request) {
		return delegate.clientRequest(request);
	}

	@Override
	public RequestContext serverReceive(Request request) {
		return delegate.serverReceive(request);
	}

	@Override
	public Span consumerSpan(MessagingRequest request) {
		return delegate.consumerSpan(request);
	}

	@Override
	public Span producerSpan(MessagingRequest request) {
		return delegate.producerSpan(request);
	}

	@Override
	public void consumerInject(Span span, MessagingRequest request) {
		delegate.consumerInject(span, request);
	}

	@Override
	public void producerInject(Span span, MessagingRequest request) {
		delegate.producerInject(span, request);
	}

	@Override
	public Span nextSpan() {
		return delegate.nextSpan();
	}

	@Override
	public boolean isNecessaryKeys(String key) {
		return delegate.isNecessaryKeys(key);
	}

	@Override
	public void injectForwardedHeaders(Setter setter) {
		delegate.injectForwardedHeaders(setter);
	}

	@Override
	public Cleaner importForwardHeaders(Getter getter) {
		return delegate.importForwardHeaders(getter);
	}

	@Override
	public void pushConfig(IPluginConfig config) {
		delegate.pushConfig(config);
	}

	@Override
	public IPluginConfig popConfig() {
		return delegate.popConfig();
	}

	@Override
	public <V> V putLocal(String key, V value) {
		return delegate.putLocal(key, value);
	}

	@Override
	public <V> V getLocal(String key) {
		return delegate.getLocal(key);
	}

	@Override
	public <T> void push(T obj) {
		delegate.push(obj);
	}

	@Override
	public <T> T pop() {
		return delegate.pop();
	}

	@Override
	public <T> T peek() {
		return delegate.peek();
	}

	@Override
	public void pushRetBound() {
		delegate.pushRetBound();
	}

	@Override
	public void popRetBound() {
		delegate.popRetBound();
	}

	@Override
	public void popToBound() {
		delegate.popToBound();
	}

	@Override
	public void clear() {
		delegate.clear();
	}

	@Override
	public void setCurrentTracing(ITracing tracing) {
		delegate.setCurrentTracing(tracing);
	}
}
