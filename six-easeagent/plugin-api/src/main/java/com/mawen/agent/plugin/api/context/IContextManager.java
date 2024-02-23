package com.mawen.agent.plugin.api.context;

import com.mawen.agent.plugin.api.InitializeContext;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface IContextManager {

	/**
	 * Get current context or create a context
	 *
	 * @return context
	 */
	InitializeContext getContext();

}
