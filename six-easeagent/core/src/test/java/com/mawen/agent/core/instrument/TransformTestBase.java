package com.mawen.agent.core.instrument;

import java.util.Set;
import java.util.function.Supplier;

import com.mawen.agent.core.plugin.interceptor.ProviderChain;
import com.mawen.agent.core.plugin.interceptor.ProviderPluginDecorator;
import com.mawen.agent.core.plugin.matcher.MethodMatcherConvert;
import com.mawen.agent.core.plugin.matcher.MethodTransformation;
import com.mawen.agent.core.plugin.registry.PluginRegistry;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.InterceptorProvider;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.matcher.IMethodMatcher;
import com.mawen.agent.plugin.matcher.MethodMatcher;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/9
 */
public class TransformTestBase {

	protected static final String FOO = "foo";
	protected static final String BAR = "bar";
	protected static final String QUX = "qux";
	protected static final String CLASS_INIT = "<cinit>";
	protected static final String FOO_STATIC = "fooStatic";

	protected Set<MethodTransformation> getMethodTransformations(int index, String methodName, InterceptorProvider provider) {
		IMethodMatcher m = MethodMatcher.builder().named(methodName).build();
		return getMethodTransformations(index, m, provider);
	}

	protected Set<MethodTransformation> getMethodTransformations(int index, IMethodMatcher m, InterceptorProvider provider) {
		ProviderChain.Builder providerBuilder = ProviderChain.builder();
		providerBuilder.addProvider(new ProviderPluginDecorator(new TestPlugin(), provider));

		MethodTransformation methodTransformation = new MethodTransformation(index, MethodMatcherConvert.INSTANCE.convert(m), providerBuilder);
		PluginRegistry.addMethodTransformation(index, methodTransformation);

		return Set.of(methodTransformation);
	}

	public static class FooInstInterceptor implements Interceptor {
		@Override
		public void before(MethodInfo methodInfo, Context context) {
			Object[] args = methodInfo.getArgs();
			args[0] = QUX;
			methodInfo.markChanged();
		}

		@Override
		public void after(MethodInfo methodInfo, Context context) {

		}

		@Override
		public int order() {
			return Order.HIGH.getOrder();
		}
	}

	public static class FooInterceptor implements Interceptor {
		@Override
		public void before(MethodInfo methodInfo, Context context) {
			Object[] args = methodInfo.getArgs();
			args[0] = QUX;
			methodInfo.markChanged();
		}

		@Override
		public void after(MethodInfo methodInfo, Context context) {
			methodInfo.setRetValue(methodInfo.getRetValue() + BAR);
		}

		@Override
		public int order() {
			return Order.HIGHEST.getOrder();
		}
	}

	public static class FooSecondInterceptor implements Interceptor {
		@Override
		public void before(MethodInfo methodInfo, Context context) {
			Object[] args = methodInfo.getArgs();
			args[0] = BAR;
			methodInfo.markChanged();
		}

		@Override
		public void after(MethodInfo methodInfo, Context context) {
			methodInfo.setRetValue(methodInfo.getRetValue() + QUX);
		}

		@Override
		public int order() {
			return Order.LOW.getOrder();
		}
	}

	static class FooProvider implements InterceptorProvider {

		@Override
		public Supplier<Interceptor> getInterceptorProvider() {
			return FooInterceptor::new;
		}

		@Override
		public String getAdviceTo() {
			return "";
		}

		@Override
		public String getPluginClassName() {
			return TestPlugin.class.getCanonicalName();
		}
	}

	static class FooInstProvider implements InterceptorProvider {

		@Override
		public Supplier<Interceptor> getInterceptorProvider() {
			return FooInstInterceptor::new;
		}

		@Override
		public String getAdviceTo() {
			return "";
		}

		@Override
		public String getPluginClassName() {
			return TestPlugin.class.getCanonicalName();
		}
	}

	static class FooSecProvider implements InterceptorProvider {
		@Override
		public Supplier<Interceptor> getInterceptorProvider() {
			return FooSecondInterceptor::new;
		}

		@Override
		public String getAdviceTo() {
			return "";
		}

		@Override
		public String getPluginClassName() {
			return TestPlugin.class.getCanonicalName();
		}
	}
}
