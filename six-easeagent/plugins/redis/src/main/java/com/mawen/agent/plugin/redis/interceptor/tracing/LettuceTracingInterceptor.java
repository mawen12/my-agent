package com.mawen.agent.plugin.redis.interceptor.tracing;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.redis.RedisPlugin;
import com.mawen.agent.plugin.redis.advice.RedisChannelWriterAdvice;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = RedisChannelWriterAdvice.class, qualifier = "default", plugin = RedisPlugin.class)
public class LettuceTracingInterceptor extends CommonRedisTracingInterceptor{

	@Override
	protected void doTraceBefore(MethodInfo methodInfo, Context context) {
		Object arg0 = methodInfo.getArgs()[0];
		String cmd;
	}
}
