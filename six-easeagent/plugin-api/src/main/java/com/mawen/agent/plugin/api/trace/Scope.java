package com.mawen.agent.plugin.api.trace;

import java.io.Closeable;

/**
 * A span remains in the scope it was bound to until close it called.
 *
 * <p>This type can be extended so that the object graph can be built differently or overridden,
 * for example via zipkin or when mocking.
 *
 * <p>The scope must be close after plugin:
 * <p>
 * example 1:
 * <pre>{@code
 * 	void after(...){
 * 	    RequestContext pCtx = context.get(...);
 * 	    try {
 * 	        // do business
 * 	    } finally {
 * 	        pCtx.scope().close();
 * 	    }
 * 	}
 * }</pre>
 * </p>
 * <p>
 * example 2:
 * <pre>{@code
 * 	void after(...){
 * 	    RequestContext pCtx = context.get(...);
 * 	    try (Scope scope = pCtx.scope()) {
 * 	        // do business
 * 	    }
 * 	}
 * }</pre>
 * </p>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface Scope extends Closeable {

	/**
	 * No exceptions are thrown when unbinding a span scope.
	 * It must be called after your business.
	 *
	 * <pre>{@code
	 * 	try {
	 * 	    // do business
	 * 	} finally {
	 * 	    scope.close();
	 * 	}
	 * }</pre>
	 */
	@Override
	void close();

	/**
	 * Returns the underlying Scope object or {@code null} if there is none.
	 * Here is a Scope objects: {@code brave.scope }
	 */
	Object unwrap();
}
