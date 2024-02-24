package com.mawen.agent.plugin.report;

/**
 * A callback of a single result or error.
 *
 * <p>This is a bridge to async libraries such as CompletableFuture complete, completeExceptionally.
 *
 * <p>Implementations will call either {@link #onSuccess(Object)} or {@link #onError(Throwable)}, but not both.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface Callback<V> {

	/**
	 * Invoked when computation produces its potentially null value successfully.
	 *
	 * <p>When this is called, {@link #onError(Throwable)} won't be.
	 */
	void onSuccess(V value);

	/**
	 * Invoked when computation produces a possibly null value successfully.
	 *
	 * <p>When this is called, {@link #onSuccess(Object)} won't be.
	 */
	void onError(Throwable t);
}
