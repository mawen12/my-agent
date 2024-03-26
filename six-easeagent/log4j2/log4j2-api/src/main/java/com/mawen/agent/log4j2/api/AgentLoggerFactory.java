package com.mawen.agent.log4j2.api;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Logger;

import javax.annotation.Nonnull;

import com.mawen.agent.log4j2.ClassLoaderSupplier;
import com.mawen.agent.log4j2.exception.Log4j2Exception;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/22
 */
public class AgentLoggerFactory<T extends AgentLogger> {
	private final AgentLogger agentLogger;
	private final ClassLoader classLoader;
	private final Object factory;
	private final Method method;
	private final Function<Logger, T> loggerSupplier;
	private final Mdc mdc;

	private AgentLoggerFactory(@Nonnull ClassLoader classLoader,
	                          @Nonnull Object factory, @Nonnull Method method,
	                          @Nonnull Function<Logger, T> loggerSupplier, @Nonnull Mdc mdc) {
		this.classLoader = classLoader;
		this.factory = factory;
		this.method = method;
		this.loggerSupplier = loggerSupplier;
		this.mdc = mdc;
		this.agentLogger = this.getLogger(AgentLoggerFactory.class.getName());
	}

	public static <T extends AgentLogger> Builder<T> builder(ClassLoaderSupplier classLoaderSupplier,
	                                                         Function<Logger, T> loggerSupplier,
	                                                         Class<T> tClass) {
		ClassLoader classLoader = Objects.requireNonNull(classLoaderSupplier.get(), "classLoader must not be null");
		return new Builder<>(classLoader, loggerSupplier, tClass);
	}

	public <N extends AgentLogger> AgentLoggerFactory<N> newFactory(Function<Logger, N> loggerSupplier, Class<N> tClass) {
		try {
			return new Builder<N>(classLoader, loggerSupplier, tClass).build();
		}
		catch (Exception e) {
			agentLogger.error("new factory fail: {}", e);
		}
		return null;
	}

	public T getLogger(String name) {
		ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(classLoader);
			Object o = method.invoke(factory, name);
			Thread.currentThread().setContextClassLoader(oldClassLoader);
			java.util.logging.Logger logger = (java.util.logging.Logger) o;
			return loggerSupplier.apply(logger);
		}
		catch (Exception e) {
			throw new Log4j2Exception(e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(oldClassLoader);
		}

	}

	public Mdc mdc() {
		return mdc;
	}

	public static class Builder<T extends AgentLogger> {
		private  final ClassLoader classLoader;
		private final Function<Logger, T> loggerSupplier;
		private final Class<T> tClass;

		public Builder(@Nonnull ClassLoader classLoader, @Nonnull Function<Logger, T> loggerSupplier, @Nonnull Class<T> tClass) {
			this.classLoader = classLoader;
			this.loggerSupplier = loggerSupplier;
			this.tClass = tClass;
		}

		public AgentLoggerFactory<T> build() {
			ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
			try {
				Thread.currentThread().setContextClassLoader(classLoader);
				Class<?> aClass = classLoader.loadClass("com.mawen.agent.log4j2.impl.LoggerProxyFactory");
				Class<?> parameterTypes = classLoader.loadClass(String.class.getName());
				Constructor<?> constructor = aClass.getDeclaredConstructor(String.class);
				Object factory = constructor.newInstance(tClass.getName());
				Method method = aClass.getDeclaredMethod("getAgentLogger", parameterTypes);
				return new AgentLoggerFactory<>(classLoader, factory, method, loggerSupplier, buildMdc());
			}
			catch (Exception e) {
				throw new RuntimeException(e);
			}
			finally {
				Thread.currentThread().setContextClassLoader(oldClassLoader);
			}
		}

		@SuppressWarnings("unchecked")
		private Mdc buildMdc() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
			Class<?> mdcClass = classLoader.loadClass("com.mawen.agent.log4j2.impl.MdcProxy");

			Field putField = mdcClass.getDeclaredField("PUT_INSTANCE");
			BiFunction<String, String, Void> put = (BiFunction<String, String, Void>) putField.get(null);

			Field removeField = mdcClass.getDeclaredField("REMOVE_INSTANCE");
			Function<String, Void> remove = (Function<String, Void>) removeField.get(null);

			Field getField = mdcClass.getDeclaredField("GET_INSTANCE");
			Function<String, String> get = (Function<String, String>) getField.get(null);

			return new Mdc(put, remove, get);
		}
	}
}
