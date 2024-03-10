package com.mawen.agent.core.plugin;

import com.mawen.agent.core.plugin.annotation.Index;
import com.mawen.agent.core.plugin.transformer.advice.support.NoExceptionHandler;
import com.mawen.agent.plugin.api.InitializeContext;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/6
 */
public class CommonInlineAdvice {

	private static final String CONTEXT = "agent_context";
	private static final String POS = "agent_pos";

	@Advice.OnMethodEnter(suppress = NoExceptionHandler.class)
	public static MethodInfo enter(@Index int index,
	                               @Advice.This(optional = true) Object invoker,
	                               @Advice.Origin("#t") String type,
	                               @Advice.Origin("#m") String method,
	                               @Advice.AllArguments(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object[] args,
	                               @Advice.Local(CONTEXT) InitializeContext context) {
		context = Agent.initializeContextSupplier.getContext();
		if (context.isNoop()) {
			return null;
		}

		var methodInfo = MethodInfo.builder()
				.invoker(invoker)
				.type(type)
				.method(method)
				.args(args)
				.build();
		Dispatcher.enter(index, methodInfo, context);
		if (methodInfo.isChanged()) {
			args = methodInfo.getArgs();
		}

		return methodInfo;
	}

	@Advice.OnMethodExit(onThrowable = Exception.class, suppress = NoExceptionHandler.class)
	public static void exit(@Index int index,
	                        @Advice.Enter MethodInfo methodInfo,
	                        @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
	                        @Advice.Thrown(readOnly = false, typing = Assigner.Typing.DYNAMIC) Throwable throwable,
	                        @Advice.Local(CONTEXT) InitializeContext context) {
		if (context.isNoop()) {
			return;
		}

		methodInfo.throwable(throwable);
		methodInfo.retValue(result);
		Dispatcher.exit(index, methodInfo, context);
		if (methodInfo.isChanged()) {
			result = methodInfo.getRetValue();
		}
	}

	@Advice.OnMethodExit(suppress = NoExceptionHandler.class)
	public static void exit(@Index int index,
	                        @Advice.This(optional = true) Object invoker,
	                        @Advice.Enter MethodInfo methodInfo,
	                        @Advice.Return(readOnly = false, typing = Assigner.Typing.DYNAMIC) Object result,
	                        @Advice.Local(CONTEXT) InitializeContext context) {
		if (context.isNoop()) {
			return;
		}

		methodInfo.setInvoker(invoker);
		methodInfo.setRetValue(result);
		Dispatcher.exit(index, methodInfo, context);
		if (methodInfo.isChanged()) {
			result = methodInfo.getRetValue();
		}
	}
}
