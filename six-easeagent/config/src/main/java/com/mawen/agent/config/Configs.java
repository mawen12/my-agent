package com.mawen.agent.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.utils.NoNull;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class Configs implements Config {

	protected Map<String, String> source;

	protected Configs() {
	}

	public Configs(Map<String, String> source) {
		this.source = new TreeMap<>(source);
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
		return NoNull.of(this.source.get(name), defVal);
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
		return NoNull.of(getInt(name), defVal);
	}

	@Override
	public Boolean getBoolean(String name) {
		String value = this.source.get(name);
		if (value == null) {
			return false;
		}
		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
	}

	@Override
	public Boolean getBoolean(String name, boolean defVal) {
		return NoNull.of(getBoolean(name), defVal);
	}

	@Override
	public Boolean getBooleanNullForUnset(String name) {
		String value = this.source.get(name);

		return value != null
				? value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("false")
				: null;
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
		return NoNull.of(getDouble(name), defVal);
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
		return NoNull.of(getLong(name), defVal);
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
	public Set<String> keySet() {
		return this.source.keySet();
	}

	@Override
	public Map<String, String> getConfigs() {
		return new TreeMap<>(this.source);
	}


	public void mergeConfigs(Configs configs) {
		Map<String, String> merged = configs.getConfigs();
		if (merged.isEmpty()) {
			return;
		}

		this.source.putAll(merged);
	}
}
