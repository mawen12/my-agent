package com.mawen.agent.plugin.api;

import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.trace.TracingContext;
import com.mawen.agent.plugin.bridge.NoOpIPluginConfig;

/**
 * Subtype of {@link Context} and {@link TracingContext} which can
 * push and pop Config.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface InitializeContext extends TracingContext {

	/**
	 * Pushes a Config onto the top of session context config stack.
	 *
	 * @param config the config to be pushed onto this stack.
	 */
	void pushConfig(IPluginConfig config);

	/**
	 * Removes the Config at the top of this session context config stack
	 * and returns that config as the value of this function.
	 *
	 * @return The config at the top of this stack (the last config of the <tt>Config</tt> object)
	 * return {@link NoOpIPluginConfig#INSTANCE} if the stack is empty.
	 */
	IPluginConfig popConfig();

	/**
	 * Unlike get/put method transfer cross different interceptors and event cross the whole session,
	 * putLocal/getLocal can only transfer data in current interceptor instance.
	 * eg. when putLocal is called to put a Span in an interceptor's 'before' method,
	 * it can only be accessed in current interceptor by 'getLocal', and can't access or modify by other interceptors.
	 *
	 * @param key the key whose associated value is to returned
	 * @param value the value to which the specified key is mapped, or
	 *              {@code null} if this context contains no mapping for the key
	 * @return the value
	 */
	<V> V putLocal(String key, V value);

	<V> V getLocal(String key);

	/**
	 * Push/pop/peek a object onto the top of session context retStack.
	 * usages: push a Span to context when an interceptor's 'before' called,
	 * and pop the Span in 'after' procession
	 */
	<T> void push(T obj);

	<T> T pop();

	<T> T peek();

	/**
	 * called by framework to maintain stack
	 */
	void pushRetBound();

	/**
	 * called by framework to maintain stack
	 */
	void popRetBound();

	/**
	 * called by framework to maintain stack
	 */
	void popToBound();

	/**
	 * clear the context
	 */
	void clear();
}
