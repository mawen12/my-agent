package com.mawen.agent.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.Const;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.config.PluginConfigChangeListener;
import com.mawen.agent.plugin.utils.common.StringUtils;
import org.checkerframework.common.reflection.qual.NewInstance;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public class PluginConfig implements IPluginConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(PluginConfig.class);

	private final Set<PluginConfigChangeListener> listeners;
	private final String domain;
	private final String namespace;
	private final String id;
	private final Map<String, String> global;
	private final Map<String, String> cover;
	private final boolean enabled;

	public PluginConfig(Set<PluginConfigChangeListener> listeners, String domain, String namespace, String id,
	                    Map<String, String> global, Map<String, String> cover) {
		this.listeners = listeners;
		this.domain = domain;
		this.namespace = namespace;
		this.id = id;
		this.global = global;
		this.cover = cover;
		Boolean b = getBoolean(Const.ENABLED_CONFIG);
		if (b == null) {
			enabled = false;
		} else {
			enabled = b;
		}
	}

	public static PluginConfig build(String domain, String id, Map<String, String> global, String namespace,
	                                 Map<String, String> cover, PluginConfig oldConfig) {
		Set<PluginConfigChangeListener> listeners;
		if (oldConfig == null) {
			listeners = new HashSet<>();
		} else {
			listeners = oldConfig.listeners;
		}
		return new PluginConfig(listeners, domain,namespace,id,global,cover);
	}

	@Override
	public String domain() {
		return domain;
	}

	@Override
	public String namespace() {
		return namespace;
	}

	@Override
	public String id() {
		return id;
	}

	@Override
	public boolean hasProperty(String property) {
		return global.containsKey(property) || cover.containsKey(property);
	}

	@Override
	public String getString(String property) {
		String value = cover.get(property);
		if (value != null) {
			return value;
		}
		return global.get(property);
	}

	@Override
	public Integer getInt(String property) {
		String value = this.getString(property);
		if (value == null) {
			return null;
		}
		try {
			return Integer.parseInt(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Boolean getBoolean(String property) {
		String value = cover.get(property);
		boolean implB = true;
		if (value != null) {
			implB = isTrue(value);
		}
		value = global.get(property);
		boolean globalB = false;
		if (value != null) {
			globalB = isTrue(value);
		}
		return implB && globalB;
	}

	@Override
	public Double getDouble(String property) {
		String value = this.getString(property);
		if (value == null) {
			return null;
		}
		try {
			return Double.parseDouble(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public Long getLong(String property) {
		String value = this.getString(property);
		if (value == null) {
			return null;
		}
		try {
			return Long.parseLong(value);
		}
		catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	public List<String> getStringList(String property) {
		String value = this.getString(property);
		if (StringUtils.isEmpty(value)) {
			return Collections.emptyList();
		}
		return Arrays.stream(value.split(","))
				.map(String::trim)
				.collect(Collectors.toList());
	}

	@Override
	public IPluginConfig getGlobal() {
		return new Global(domain, id, namespace, global);
	}

	@Override
	public Set<String> keySet() {
		Set<String> keys = new HashSet<>(global.keySet());
		keys.addAll(cover.keySet());
		return keys;
	}

	@Override
	public void addChangeListener(PluginConfigChangeListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}

	public void foreachConfigChangeListener(Consumer<PluginConfigChangeListener> action) {
		Set<PluginConfigChangeListener> oldListeners;
		synchronized (listeners) {
			oldListeners = new HashSet<>(listeners);
		}
		for (PluginConfigChangeListener oldListener : oldListeners) {
			try {
				action.accept(oldListener);
			}
			catch (Exception e) {
				LOGGER.error("PluginConfigChangeListener<{}> change plugin config fail : {}",oldListener.getClass(), e.getMessage());
			}
		}
	}

	private boolean isTrue(String value) {
		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
	}

	public class Global extends PluginConfig implements IPluginConfig {

		public Global(String domain, String namespace, String id, Map<String, String> global) {
			super(Collections.emptySet(), domain, namespace, id, global, Collections.emptyMap());
		}

		@Override
		public void addChangeListener(PluginConfigChangeListener listener) {
			PluginConfig.this.addChangeListener(listener);
		}

		@Override
		public void foreachConfigChangeListener(Consumer<PluginConfigChangeListener> action) {
			PluginConfig.this.foreachConfigChangeListener(action);
		}
	}
}
