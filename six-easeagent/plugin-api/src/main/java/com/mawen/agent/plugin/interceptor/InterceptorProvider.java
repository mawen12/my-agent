package com.mawen.agent.plugin.interceptor;

import java.util.function.Supplier;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public interface InterceptorProvider {

	Supplier<Interceptor> getInterceptorProvider();

	String getAdviceTo();

	String getPluginClassName();
}
