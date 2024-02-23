package com.mawen.agent.plugin.api.context;

import java.util.Map;

import com.mawen.agent.plugin.api.Cleaner;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.trace.SpanContext;

/**
 * An asynchronous thread snapshot context
 * <p>
 * code example:
 * <pre>{@code
 * 	AsyncContext asyncContext = context.exportAsync();
 * 	class Run implements Runnable {
 * 	    void run() {
 * 	        try (Cleaner cleaner = asyncContext.importToCurrent()) {
 * 	            // do something
 * 	            // or Agent.getContext().nextSpan();
 * 	        }
 * 	    }
 * 	}
 * }</pre>
 * </p>
 *
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface AsyncContext {

	/**
	 * When true, do nothing anything and nothing is reported.
	 * However, this Tracing should still be injected into outgoing requests.
	 * Use this flag to avoid performing expensive computation.
	 */
	boolean isNoop();

	/**
	 * get Span Context for Tracing
	 *
	 * @return SpanContext
	 */
	SpanContext getSpanContext();

	/**
	 * Import this AsyncContext to current {@link Context} and return a {@link Cleaner}.
	 *
	 * <p>
	 * The Cleaner must be close after business:
	 * example:
	 * <pre>{@code
	 * 	void callback(AsyncContext ac){
	 * 	    try(Cleaner cleaner = ac.importAsync()){
	 * 	        // do business
	 * 	    }
	 * 	}
	 * }</pre>
	 * </p>
	 *
	 * @return {@link Cleaner}
	 */
	Cleaner importToCurrent();

	/**
	 * @return all async snapshot context key:value
	 */
	Map<Object, Object> getAll();

	/**
	 * Returns the value to which the specified key is mapped,
	 * or {@code null} if this context contains no mapping for the key.
	 *
	 * <p>More formally, if this context contains a mapping from a key
	 * {@code k} to a value {@code v} such that {@code (key == null? k == null : key.equals(k))},
	 * then this method returns {@code v}; otherwise it returns {@code null}. (There can be at most one such mapping.)
	 *
	 * <p>If this context contains permits null values, then a return value of {@code null}
	 * does not <i>necessarily</i> indicate that the context contains no mapping for the key;
	 * it's also possible that the context explicitly maps the key to {@code null}
	 *
	 * @param key the key whose associated value is to be returned
	 * @return the value to which the specified key is mapped, or {@code null} if this context contains no mapping for the key
	 * @throws ClassCastException if the key is of an inappropriate type for this context
	 *                             (<a href="{@docRoot}/java/util/Collection.html#optional-restrictions">optional</a>)
	 */
	<T> T get(Object key);

	/**
	 * Associates the specified value with the specified key in this context (optional operation).
	 * If the context previously contained a mapping for the key, the old value is replaced by the specified value.
	 * (A context <tt>m</tt> is said to contain a mapping for a key <tt>k</tt>).
	 *
	 * @param key key with which the specified value is to be associated
	 * @param value value to be associated with the specified key
	 * @return the previous value associated with <tt>key</tt>, or <tt>null</tt> if there was no mapping for <tt>key</tt>.
	 * (A <tt>null</tt> return can also indicate the context if the implementation supports <tt>null</tt> values.)
	 * @throws ClassCastException if the class of the specified key or value prevents it from being stored in this context
	 */
	<V> V put(Object key, V value);
}
