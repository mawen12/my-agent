package com.mawen.agent.plugin.async;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import javax.annotation.Nullable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class ThreadLocalCurrentContext {
	public static final ThreadLocalCurrentContext DEFAULT = new ThreadLocalCurrentContext(new InheritableThreadLocal<>());

	final ThreadLocal<Context> local;
	final RevertToNullScope revertToNull;

	public ThreadLocalCurrentContext(ThreadLocal<Context> local) {
		if (local == null) {
			throw new NullPointerException("local == null");
		}
		this.local = local;
		this.revertToNull = new RevertToNullScope(local);
	}

	public Context get() {
		return local.get();
	}

	public Scope newScope(@Nullable Context current) {
		final var previous = local.get();
		local.set(current);
		return previous != null ? new RevertToPreviousScope(local,previous) : revertToNull;
	}

	public Scope maybeScope(@Nullable Context context) {
		var current = get();
		if (Objects.equals(current, context)) {
			return Scope.NOOP;
		}
		return newScope(context);
	}

	public void fill(BiConsumer<String, String> consumer,  String[] names) {
		final var ctx = get();
		if (ctx != null) {
			for (var one : names) {
				consumer.accept(one, ctx.get(one));
			}
		}
	}

	/**
	 * Wraps the input so that it executes with the same context as now.
	 */
	public Runnable wrap(Runnable task) {
		final var invocationContext = get();
		return new CurrentContextRunnable(this, invocationContext, task);
	}

	public static boolean isWrapped(Runnable task) {
		return task instanceof CurrentContextRunnable;
	}

	public static Context createContext(String... kvs) {
		if (kvs.length % 2 != 0) {
			throw new IllegalArgumentException("size of kvs should be even number");
		}
		final var ctx = new Context();
		for (var i = 0; i < kvs.length; i += 2) {
			ctx.put(kvs[i], kvs[i + 1]);
		}
		return ctx;
	}

	public record CurrentContextRunnable(ThreadLocalCurrentContext threadLocalCurrentContext, Context ctx, Runnable original) implements Runnable {

		@Override
		public void run() {
			try (Scope scope = this.threadLocalCurrentContext.maybeScope(ctx)) {
				original.run();
			}
		}
	}

	public interface Scope extends Closeable {
		Scope NOOP = new NOOPScope();

		@Override
		void close();
	}

	public static class NOOPScope implements Scope {
		@Override
		public void close() {}

		@Override
		public String toString() {
			return "NoopScope";
		}
	}

	public static class Context {
		private final Map<String, String> data = new HashMap<>();

		public String put(String key, String value) {
			return data.put(key, value);
		}

		public String get(String key) {
			return data.get(key);
		}

		public boolean containsKey(String key) {
			return data.containsKey(key);
		}

		@Override
		public String toString() {
			return data.toString();
		}
	}

	record RevertToNullScope(ThreadLocal<Context> local) implements Scope {

		@Override
		public void close() {
			local.set(null);
		}
	}

	static final record RevertToPreviousScope(ThreadLocal<Context> local,  Context previous) implements Scope {

		@Override
		public void close() {
			local.set(previous);
		}
	}
}
