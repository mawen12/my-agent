package com.mawen.agent.plugin.api.metric;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public class ServiceMetricRegistry {
	private static final ConcurrentHashMap<Key, ServiceMetric> INSTANCES = new ConcurrentHashMap<>();

	/**
	 * Obtain an ServiceMetric when it is already registered.
	 * If you have not registered, create one and return.
	 * The registered {@link ServiceMetricRegistry.Key} is domain, namespace, id, tags and the type by the supplier.
	 *
	 * @param config {@link IPluginConfig} domain, namespace from id from
	 * @param tags {@link Tags} metric tags
	 * @param supplier {@link ServiceMetric} Instance Supplier
	 * @return the type of ServiceMetric by the Supplier
	 */
	public static <T extends ServiceMetric> T getOrCreate(IPluginConfig config,  Tags tags, ServiceMetricSupplier<T> supplier) {
		return getOrCreate(config.domain(), config.namespace(), config.id(), tags, supplier);
	}

	/**
	 * Obtain an ServiceMetric when it is already registered.
	 * If you have not registered, create one and return.
	 * The registered {@link ServiceMetricRegistry.Key} is domain, namespace, id, tags and the type by the supplier.
	 *
	 * @param domain String
	 * @param namespace String
	 * @param id String
	 * @param tags {@link Tags} metric tags
	 * @param supplier {@link ServiceMetric} Instance Supplier
	 * @return the type of ServiceMetric by the Supplier
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ServiceMetric> T getOrCreate(String domain, String namespace, String id, Tags tags, ServiceMetricSupplier<T> supplier) {
		Key key = new Key(domain, namespace, id, tags, supplier.getType());
		ServiceMetric metric = INSTANCES.get(key);
		if (metric != null) {
			return (T) metric;
		}
		synchronized (INSTANCES) {
			metric = INSTANCES.get(key);
			if (metric != null) {
				return (T) metric;
			}
			IPluginConfig config = Agent.getConfig(domain, namespace, id);
			NameFactory nameFactory = supplier.newNameFactory();
			MetricRegistry metricRegistry = Agent.newMetricRegistry(config, nameFactory, tags);
			T newMetric = supplier.newInstance(metricRegistry, nameFactory);
			INSTANCES.put(key, newMetric);
			return newMetric;
		}
	}


	static class Key {
		private final int hash;
		private final String domain;
		private final String namespace;
		private final String id;
		private final Tags tags;
		private final Type type;

		public Key(String domain, String namespace, String id, Tags tags, Type type) {
			this.domain = domain;
			this.namespace = namespace;
			this.id = id;
			this.tags = tags;
			this.type = type;
			this.hash = Objects.hash(domain, namespace, id, tags, type);
		}

		@Override
		public final boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Key)) return false;

			Key key = (Key) o;
			return hash == key.hash && Objects.equals(domain, key.domain) && Objects.equals(namespace, key.namespace) && Objects.equals(id, key.id) && Objects.equals(tags, key.tags) && Objects.equals(type, key.type);
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public String toString() {
			return "Key{" +
					"hash=" + hash +
					", domain='" + domain + '\'' +
					", namespace='" + namespace + '\'' +
					", id='" + id + '\'' +
					", tags=" + tags +
					", type=" + type +
					'}';
		}
	}
}
