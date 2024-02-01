package com.mawen.agent.bytebuddy;

import java.lang.instrument.Instrumentation;

import com.mawen.agent.bytebuddy.interceptor.TimeInterceptor;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/1
 */
public class MyAgent {

	public static void premain(String agentArgs, Instrumentation inst) {
		System.out.println("This is an perform monitor agent.");

		new AgentBuilder
				.Default()
				.type(ElementMatchers.nameStartsWith("com.mawen.agent")) // 指定要拦截的类
				.transform(getTransformer())
				.with(getListener())
				.installOn(inst);
	}

	private static AgentBuilder.Transformer getTransformer() {
		return (builder, typeDescription, classLoader, javaModule, protectionDomain) -> {
			return builder
					.method(ElementMatchers.any()) // 拦截任意方法
					.intercept(MethodDelegation.to(TimeInterceptor.class)); // 委托
		};
	}

	private static AgentBuilder.Listener getListener() {
		return new AgentBuilder.Listener() {
			@Override
			public void onDiscovery(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

			}

			@Override
			public void onTransformation(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b, DynamicType dynamicType) {

			}

			@Override
			public void onIgnored(TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule, boolean b) {

			}

			@Override
			public void onError(String s, ClassLoader classLoader, JavaModule javaModule, boolean b, Throwable throwable) {

			}

			@Override
			public void onComplete(String s, ClassLoader classLoader, JavaModule javaModule, boolean b) {

			}
		};
	}

}
