package com.mawen.agent.plugin.api.config;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AutoRefreshPluginConfigRegistry {
	private static final AutoRefreshConfigSupplier<AutoRefreshPluginConfigImpl> AUTO_REFRESH_CONFIG_IMPL_SUPPLIER
			= new AutoRefreshConfigSupplier<>() {
		@Override
		public AutoRefreshPluginConfigImpl newInstance() {
			return new AutoRefreshPluginConfigImpl();
		}
	};

	private static final ConcurrentMap<Key, AutoRefreshPluginConfig> configs = new ConcurrentHashMap<>();

	/**
	 * Obtain an AutoRefreshPluginConfigImpl when it is already registered.
	 * If you have not registered, create one and return.
	 * The registered {@link Key} is domain, namespace, id.
	 *
	 * @param domain String
	 * @param namespace String
	 * @param id String
	 * @return {@link AutoRefreshPluginConfigImpl}
	 */
	public static AutoRefreshPluginConfigImpl getOrCreate(String domain, String namespace, String id) {
		return getOrCreate(domain,namespace,id,AUTO_REFRESH_CONFIG_IMPL_SUPPLIER);
	}

	public static <C extends AutoRefreshPluginConfig> C getOrCreate(String domain, String namespace,
	                                                                String id, AutoRefreshConfigSupplier<C> supplier) {
		Key key = new Key(domain, namespace, id, supplier.getType());
		AutoRefreshPluginConfig autoRefreshConfig = configs.get(key);
		if (autoRefreshConfig != null) {
			triggerChange(domain,namespace, id,autoRefreshConfig);
			return (C) autoRefreshConfig;
		}
		synchronized (configs) {
			autoRefreshConfig = configs.get(key);
			if (autoRefreshConfig != null) {
				triggerChange(domain, namespace,id,autoRefreshConfig);
				return (C) autoRefreshConfig;
			}
			C newConfig = supplier.newInstance();
			triggerChange(domain, namespace, id, newConfig);
			configs.put(key, newConfig);
			return newConfig;
		}
	}

	private static <C extends AutoRefreshPluginConfig> void triggerChange(String domain, String namespace, String id, C newConfig) {
		IPluginConfig config = Agent.getConfig(domain, namespace, id);
		newConfig.onChange(null,config);
		config.addChangeListener(newConfig);
	}

	static class Key {
		private final String domain;
		private final String namespace;
		private final String id;
		private final Type type;

		public Key(String domain, String namespace, String id, Type type) {
			this.domain = domain;
			this.namespace = namespace;
			this.id = id;
			this.type = type;
		}

		@Override
		public final boolean equals(Object o) {
			if (this == o) return true;
			if (!(o instanceof Key key)) return false;

			return Objects.equals(domain, key.domain) && Objects.equals(namespace, key.namespace) && Objects.equals(id, key.id) && Objects.equals(type, key.type);
		}

		@Override
		public int hashCode() {
			int result = Objects.hashCode(domain);
			result = 31 * result + Objects.hashCode(namespace);
			result = 31 * result + Objects.hashCode(id);
			result = 31 * result + Objects.hashCode(type);
			return result;
		}

		@Override
		public String toString() {
			return "Key{" +
					"domain='" + domain + '\'' +
					", namespace='" + namespace + '\'' +
					", id='" + id + '\'' +
					", type=" + type +
					'}';
		}
	}
}
