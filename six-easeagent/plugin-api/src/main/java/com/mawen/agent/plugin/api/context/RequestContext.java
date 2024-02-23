package com.mawen.agent.plugin.api.context;

import java.util.Map;

import com.mawen.agent.plugin.api.trace.Response;
import com.mawen.agent.plugin.api.trace.Scope;
import com.mawen.agent.plugin.api.trace.Setter;
import com.mawen.agent.plugin.api.trace.Span;

/**
 * A cross-process data context, including tracing and Forwarded Headers
 * <p>
 * The scope must be close after plugin:
 * <pre>{@code
 * 	void after(...){
 * 	    RequestContext rc = context.get(...);
 * 	    try {
 * 	        ...
 * 	    } finally {
 * 	        rc.scope().close();
 * 	    }
 * 	}
 * }</pre>
 * </p>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface RequestContext extends Setter {

	/**
	 * When true, do nothing anything and nothing is reported.
	 * However, this Tracing should still be injected into outgoing requests.
	 * Use this flag to avoid performing expensive computation.
	 */
	boolean isNoop();

	/**
	 * @return {@link Span} for next progress client span
	 */
	Span span();

	/**
	 * The scope must be close after plugin:
	 * <pre>{@code
	 * 	void after(...){
	 * 	    RequestContext rc = context.get(...);
	 * 	    try {
	 * 	        ...
	 * 	    } finally {
	 * 	        rc.scope().close();
	 * 	    }
	 * 	}
	 * }</pre>
	 *
	 * @return {@link Scope} for current Span
	 */
	Scope scope();

	/**
	 * set header for next progress
	 *
	 * @param name header name
	 * @param value header value
	 */
	void setHeader(String name, String value);

	/**
	 * @return headers from the progress data context
	 */
	Map<String, String> getHeaders();

	/**
	 * finish the progress span and save tag from {@link Response#header(String)}
	 *
	 * @param response {@link Response}
	 */
	void finish(Response response);

}
