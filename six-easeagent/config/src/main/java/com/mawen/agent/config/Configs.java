package com.mawen.agent.config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.api.config.ChangeItem;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigChangeListener;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class Configs implements Config {
	private static final Logger LOGGER = LoggerFactory.getLogger(Configs.class);

	protected Map<String ,String> source;
	protected ConfigNotifier notifier;

	protected Configs() {

	}

	public Configs(Map<String, String> source) {
		this.source = new TreeMap<>(source);
		notifier = new ConfigNotifier("");
	}

	@Override
	public boolean hasPath(String path) {
		return this.source.containsKey(path);
	}

	@Override
	public String getString(String name) {
		return this.source.get(name);
	}

	@Override
	public String getString(String name, String defVal) {
		String val = this.source.get(name);

		return val == null ? defVal : val;
	}

	@Override
	public Integer getInt(String name) {
		String value = this.source.get(name);
		if (value == null) {
			return null;
		}

		try {
			return Integer.parseInt(value);
		}
		catch (Exception e) {
			return null;
		}
	}

	@Override
	public Integer getInt(String name, int defVal) {
		Integer anInt = getInt(name);
		return anInt == null ? defVal : anInt;
	}

	@Override
	public Boolean getBoolean(String name) {
		String value = this.source.get(name);
		if (value == null) {
			return null;
		}
		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
	}

	@Override
	public Boolean getBoolean(String name, boolean defVal) {
		Boolean aBoolean = getBoolean(name);
		return aBoolean == null ? defVal : aBoolean;
	}

	@Override
	public Boolean getBooleanNullForUnset(String name) {
		String value = this.source.get(name);
		if (value == null) {
			return null;
		}

		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("false");
	}

	@Override
	public Double getDouble(String name) {
		String value = this.source.get(name);
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
	public Double getDouble(String name, double defVal) {
		Double anDouble = getDouble(name);
		return anDouble == null ? defVal : anDouble;
	}

	@Override
	public Long getLong(String name) {
		String value = this.source.get(name);
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
	public Long getLong(String name, long defVal) {
		Long aLong = getLong(name);
		return aLong == null ? defVal : aLong;
	}

	@Override
	public List<String> getStringList(String name) {
		String value = this.source.get(name);
		if (value == null) {
			return null;
		}
		return Arrays.stream(value.split(",")).filter(Objects::nonNull).collect(Collectors.toList());
	}

	@Override
	public Runnable addChangeListener(ConfigChangeListener listener) {
		return this.notifier.addChangeListener(listener);
	}

	@Override
	public Set<String> keySet() {
		return this.source.keySet();
	}

	@Override
	public Map<String, String> getConfigs() {
		return new TreeMap<>(this.source);
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {
		Map<String, String> dump = new TreeMap<>(this.source);
		List<ChangeItem> items = new LinkedList<>();
		changes.forEach((name, value) -> {
			String old = dump.get(name);
			if (!Objects.equals(old, value)) {
				dump.put(name, value);
				items.add(new ChangeItem(name, name, old, value));
			}
		});
		if (!items.isEmpty()) {
			LOGGER.info("change items: {}",items);
			this.source = dump;
			this.notifier.handleChanges(items);
		}
	}

	@Override
	public void updateConfigsNotNotify(Map<String, String> changes) {
		this.source.putAll(changes);
	}

	protected boolean hasText(String text) {
		return text != null && text.trim().length() > 0;
	}
}
