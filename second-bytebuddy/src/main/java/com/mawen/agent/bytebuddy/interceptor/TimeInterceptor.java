package com.mawen.agent.bytebuddy.interceptor;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class TimeInterceptor {

	@RuntimeType
	public static Object intercept(@Origin Method method, @SuperCall Callable<?> callable) throws Exception {
		long start = System.currentTimeMillis();
		try {
			// 原有函数执行
			return callable.call();
		}
		finally {
			System.out.println(method + ": took " + (System.currentTimeMillis() - start) + "ms");
		}
	}

}
