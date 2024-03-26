package com.mawen.agent.plugin.api.config;

import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.mawen.agent.plugin.bridge.Agent;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/24
 */
public class AutoRefreshPluginConfigRegistry {
	private static final AutoRefreshConfigSupplier<AutoRefreshPluginConfigImpl> AUTO_REFRESH_CONFIG_IMPL_SUPPLIER = new AutoRefreshConfigSupplier<AutoRefreshPluginConfigImpl>() {

		@Override
		public AutoRefreshPluginConfigImpl newInstance() {
			return new AutoRefreshPluginConfigImpl();
		}
	};

	private static final ConcurrentMap<Key, IPluginConfig> configs = new ConcurrentHashMap<>();

	/**
	 * Obtain an AutoRefreshPluginConfigImpl when it is already registered.
	 * If you have not registered, create one and return.
	 * The registered {@link Key} is domain, namespace, id.
	 *
	 * @param domain    String
	 * @param namespace String
	 * @param id        String
	 * @return {@link AutoRefreshPluginConfigImpl}
	 */
	public static AutoRefreshPluginConfigImpl getOrCreate(String domain, String namespace, String id) {
		return getOrCreate(domain, namespace, id, AUTO_REFRESH_CONFIG_IMPL_SUPPLIER);
	}

	@SuppressWarnings("unchecked")
	public static <C extends AutoRefreshPluginConfigImpl> C getOrCreate(String domain, String namespace,
	                                                      String id, AutoRefreshConfigSupplier<C> supplier) {
		Key key = new Key(domain, namespace, id, supplier.getType());
		IPluginConfig autoRefreshConfig = configs.get(key);
		if (autoRefreshConfig != null) {
			triggerChange(domain, namespace, id, autoRefreshConfig);
		}

		synchronized (configs) {
			autoRefreshConfig = configs.get(key);
			if (autoRefreshConfig != null) {
				triggerChange(domain, namespace, id, autoRefreshConfig);
				return (C) autoRefreshConfig;
			}
			C newConfig = supplier.newInstance();
			triggerChange(domain, namespace, id, newConfig);
			configs.put(key, newConfig);
			return newConfig;
		}
	}

	private static <C extends IPluginConfig> void triggerChange(String domain, String namespace, String id, C newConfig) {
		IPluginConfig config = Agent.getConfig(domain, namespace, id);
		if (newConfig instanceof AutoRefreshPluginConfigImpl) {
			((AutoRefreshPluginConfigImpl)newConfig).setConfig(config);
		}
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

		public String domain() {
			return domain;
		}

		public String namespace() {
			return namespace;
		}

		public String id() {
			return id;
		}

		public Type type() {
			return type;
		}
	}
}
