package com.mawen.agent.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.mawen.agent.config.report.ReportConfigAdapter;
import com.mawen.agent.plugin.api.config.ConfigConst;
import lombok.Getter;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/26
 */
@Getter
public class GlobalConfigs extends Configs implements ConfigManagerMXBean{

	Configs originalConfigs;

	public GlobalConfigs(Map<String, String> source) {
		super();
		this.originalConfigs = new Configs(source);
		// reporter adapter
		var map = new TreeMap<>(source);
		ReportConfigAdapter.convertConfig(map);
		// check environment config
		this.source = new TreeMap<>(map);
		this.notifier = new ConfigNotifier("");
	}

	@Override
	public void updateService(String json, String version) throws IOException {
		this.updateConfigs(ConfigUtils.json2KVMap(json));
	}

	@Override
	public void updateCanary(String json, String version) throws IOException {
		Map<String, String> originals = ConfigUtils.json2KVMap(json);
		HashMap<String, String> rst = new HashMap<>();
		originals.forEach((k, v) -> rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v));
		this.updateConfigs(rst);
	}

	@Override
	public void updateService2(Map<String, String> configs, String version) {
		this.updateConfigs(configs);
	}

	@Override
	public void updateCanary2(Map<String, String> configs, String version) {
		var rst = new HashMap<String, String>();
		for (var entry : configs.entrySet()) {
			var k = entry.getKey();
			var v = entry.getValue();
			rst.put(ConfigConst.join(ConfigConst.GLOBAL_CANARY_LABELS, k), v);
		}
		this.updateConfigs(CompatibilityConversion.transform(rst));
	}

	@Override
	public List<String> availableConfigNames() {
		throw new UnsupportedOperationException();
	}

	public void mergeConfigs(GlobalConfigs configs) {
		var merged = configs.getOriginalConfigs().getConfigs();
		if (merged.isEmpty()) {
			return;
		}
		this.updateConfigsNotNotify(merged);
	}

	public String toPrettyDisplay() {
		return this.source.toString();
	}

	public String getString(String name) {
		return this.source.get(name);
	}

	public String getString(String name, String defaultValue) {
		var val = this.source.get(name);
		return val == null ? defaultValue : val;
	}

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
}
