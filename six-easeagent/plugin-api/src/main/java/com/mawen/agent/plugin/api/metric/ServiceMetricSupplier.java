package com.mawen.agent.plugin.api.metric;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.mawen.agent.plugin.api.metric.name.NameFactory;

/**
 * A {@link ServiceMetric} Supplier
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 * @param <T> the type of ServiceMetric by this Supplier
 */
public abstract class ServiceMetricSupplier<T extends ServiceMetric> {

	private final Type type;

	public ServiceMetricSupplier() {
		Type superClass = getClass().getGenericSuperclass();
		if (superClass instanceof Class<?>) {
			throw new IllegalArgumentException("Internal error: TypeReference constructed without actual type information");
		}
		type = ((ParameterizedType) superClass).getActualTypeArguments()[0];
	}

	/**
	 * the type of ServiceMetric
	 *
	 * @return {@link Type}
	 */
	public Type getType() {
		return type;
	}

	public abstract NameFactory newNameFactory();

	/**
	 * new a ServiceMetric
	 *
	 * @param metricRegistry {@link MetricRegistry}
	 * @param nameFactory {@link NameFactory}
	 * @return a type of ServiceMetric
	 */
	public abstract T newInstance(MetricRegistry metricRegistry, NameFactory nameFactory);

}
