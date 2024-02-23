package com.mawen.agent.plugin.api.trace;

import com.mawen.agent.plugin.api.context.RequestContext;

/**
 * Interface Response type used for parsing and sampling.
 * Used when multi-process collaboration is needed,
 * information is extracted from the response and recorded in the {@link Span#tag(String, String)}
 * Usually used to support "ease mesh".
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 * @see RequestContext#finish(Response)
 */
public interface Response extends Getter{

	/**
	 * The method of extracting information from the response
	 */
	String header(String name);
}
