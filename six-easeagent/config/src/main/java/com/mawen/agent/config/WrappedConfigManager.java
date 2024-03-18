package com.mawen.agent.config;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.mawen.agent.plugin.async.ThreadUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
public record WrappedConfigManager(ClassLoader customClassLoader, ConfigManagerMXBean conf) implements ConfigManagerMXBean{

	@Override
	public void updateConfigs(Map<String, String> configs) {
		ThreadUtils.callWithClassLoader(customClassLoader, () -> {
			conf.updateConfigs(configs);
			return null;
		});
	}

	@Override
	public void updateService(String json, String version) throws IOException {
		try {
			ThreadUtils.callWithClassLoader(customClassLoader, () -> {
				try {
					conf.updateService(json, version);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				return null;
			});
		}
		catch (Exception e) {
			if (e.getCause() instanceof IOException ioe) {
				throw ioe;
			}
			throw e;
		}
	}

	@Override
	public void updateCanary(String json, String version) throws IOException {
		try {
			ThreadUtils.callWithClassLoader(customClassLoader,() -> {
				try {
					conf.updateCanary(json,version);
				}
				catch (IOException e) {
					throw new RuntimeException(e);
				}
				return null;
			});
		}
		catch (RuntimeException e) {
			if (e.getCause() instanceof IOException ioe) {
				throw ioe;
			}
			throw e;
		}
	}

	@Override
	public void updateService2(Map<String, String> configs, String version) {
		ThreadUtils.callWithClassLoader(customClassLoader, () -> {
			conf.updateService2(configs,version);
			return null;
		});
	}

	@Override
	public void updateCanary2(Map<String, String> configs, String version) {
		ThreadUtils.callWithClassLoader(customClassLoader, () -> {
			conf.updateCanary2(configs,version);
			return null;
		});
	}

	@Override
	public Map<String, String> getConfigs() {
		return ThreadUtils.callWithClassLoader(customClassLoader,conf::getConfigs);
	}

	@Override
	public List<String> availableConfigNames() {
		return ThreadUtils.callWithClassLoader(customClassLoader,conf::availableConfigNames);
	}
}
