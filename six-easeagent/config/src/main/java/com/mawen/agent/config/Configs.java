package com.mawen.agent.config;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.mawen.agent.plugin.api.config.Config;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/25
 */
public class Configs implements Config {

	protected Map<String ,String> source;

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
		var val = this.source.get(name);

		return val == null ? defVal : val;
	}

	@Override
	public Integer getInt(String name) {
		var value = this.source.get(name);
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
		var anInt = getInt(name);
		return anInt == null ? defVal : anInt;
	}

	@Override
	public Boolean getBoolean(String name) {
		var value = this.source.get(name);
		if (value == null) {
			return false;
		}
		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("true");
	}

	@Override
	public Boolean getBoolean(String name, boolean defVal) {
		var aBoolean = getBoolean(name);
		return aBoolean == null ? defVal : aBoolean;
	}

	@Override
	public Boolean getBooleanNullForUnset(String name) {
		var value = this.source.get(name);
		if (value == null) {
			return null;
		}

		return value.equalsIgnoreCase("yes") || value.equalsIgnoreCase("false");
	}

	@Override
	public Double getDouble(String name) {
		var value = this.source.get(name);
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
		var anDouble = getDouble(name);
		return anDouble == null ? defVal : anDouble;
	}

	@Override
	public Long getLong(String name) {
		var value = this.source.get(name);
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
		var aLong = getLong(name);
		return aLong == null ? defVal : aLong;
	}

	@Override
	public List<String> getStringList(String name) {
		var value = this.source.get(name);
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
		var merged = configs.getConfigs();
		if (merged.isEmpty()) {
			return;
		}

		this.source.putAll(merged);
	}
}
