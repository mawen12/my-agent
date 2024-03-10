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
		var key = new Key(domain, namespace, id, supplier.getType());
		var autoRefreshConfig = configs.get(key);
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
			var newConfig = supplier.newInstance();
			triggerChange(domain, namespace, id, newConfig);
			configs.put(key, newConfig);
			return newConfig;
		}
	}

	private static <C extends AutoRefreshPluginConfig> void triggerChange(String domain, String namespace, String id, C newConfig) {
		var config = Agent.getConfig(domain, namespace, id);
		newConfig.onChange(null,config);
		config.addChangeListener(newConfig);
	}

	record Key(String domain, String namespace, String id, Type type) {
	}
}
