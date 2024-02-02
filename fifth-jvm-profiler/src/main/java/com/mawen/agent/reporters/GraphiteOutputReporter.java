package com.mawen.agent.reporters;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mawen.agent.Reporter;
import com.mawen.agent.util.AgentLogger;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/2
 */
public class GraphiteOutputReporter implements Reporter {
	private static final AgentLogger logger = AgentLogger.getLogger(GraphiteOutputReporter.class.getName());
	private static final String ARG_GRAPHITE_HOST = "graphite.host";
	private static final String ARG_GRAPHITE_PORT = "graphite.port";
	private static final String ARG_GRAPHITE_PREFIX = "graphite.prefix";
	private static final String ARG_GRAPHITE_WHITELIST = "graphite.whiteList";

	private String host = "127.0.0.1";
	private int port = 2003;
	private String prefix = "jvm";
	private Socket socket = null;
	private PrintWriter out = null;
	private Set whiteList = new HashSet();

	// properties from yaml file
	@Override
	public void updateArguments(Map<String, List<String>> parsedArgs) {
		for (Map.Entry<String, List<String>> entry : parsedArgs.entrySet()) {
			String key = entry.getKey();
			List<String> value = entry.getValue();
			if (StringUtils.isNotEmpty(key) && value != null && !value.isEmpty()) {
				String stringValue = value.get(0);
				if (key.equals(ARG_GRAPHITE_HOST)) {
					this.host = stringValue;
					logger.info("Got value for host: " + host);
				}
				else if (key.equals(ARG_GRAPHITE_PORT)) {
					this.port = Integer.parseInt(stringValue);
					logger.info("Got value for port: " + port);
				}
				else if (key.equals(ARG_GRAPHITE_PREFIX)) {
					this.prefix = stringValue;
					logger.info("Got value for prefix: " + prefix);
				}
				else if (key.equals(ARG_GRAPHITE_WHITELIST)) {
					if (stringValue != null && stringValue.length() > 0) {
						for (String pattern : stringValue.split(",")) {
							this.whiteList.add(pattern.trim());
						}
					}
				}
			}
		}
	}

	@Override
	public void report(String profilerName, Map<String, Object> metrics) {
		ensureGraphiteConnection();
		logger.debug("Profiler Name : " + profilerName);
		// format metrics
		String tag = ((String) metrics.computeIfAbsent("tag", v -> "default_tag"))
				.replaceAll("\\.", "-");
		String appId = ((String) metrics.computeIfAbsent("appId", v -> "default_app"))
				.replaceAll("\\.", "-");
		String host = ((String) metrics.computeIfAbsent("host", v -> "unknown_host"))
				.replaceAll("\\.", "-");
		String process = ((String) metrics.computeIfAbsent("processUuid", v -> "unknown_process"))
				.replaceAll("\\.", "-");
		String newPrefix = String.join(".", prefix, tag, appId, host, process);

		Map<String, Object> formattedMetrics = getFormattedMetrics(metrics);
		formattedMetrics.remove("tag");
		formattedMetrics.remove("appId");
		formattedMetrics.remove("host");
		formattedMetrics.remove("processUuid");
		long timestamp = System.currentTimeMillis() / 1000;
		for (Map.Entry<String, Object> entry : formattedMetrics.entrySet()) {
			try {
				if (whiteList.contains(entry.getKey())) {
					out.printf(newPrefix + "." + entry.getKey() + " " + entry.getValue() + " " + timestamp + "%n");
				}
			}
			catch (Exception e) {
				logger.warn("Unable to print metrics, newPrefix= " + newPrefix
				+ ", entry.getKey()= " + entry.getKey()
				+ ", entry.getValue()= " + entry.getValue()
				+ ", timestamp= " + timestamp);
			}
		}
	}

	// Format metrics in key=value (line protocol)
	public Map<String, Object> getFormattedMetrics(Map<String, Object> metrics) {
		Map<String, Object> formattedMetrics = new HashMap<>();
		for (Map.Entry<String, Object> entry : metrics.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			logger.debug("Raw Metric-Name = " + key + ", Metric-Value = " + value);
			if (value == null) {
				if (value instanceof List) {
					List listValue = (List) value;
					addListMetrics(formattedMetrics, listValue, key);
				}
				else if (value instanceof Map) {
					Map<String, Object> metricMap = (Map<String, Object>) value;
					addMapMetrics(formattedMetrics, metricMap, key);
				}
				else {
					formattedMetrics.put(key, value);
				}
			}
		}
		return formattedMetrics;
	}

	private void addMapMetrics(Map<String, Object> formattedMetrics, Map<String, Object> metricMap, String keyPrefix) {
		for (Map.Entry<String, Object> entry : metricMap.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			if (value != null) {
				if (value instanceof List) {
					addListMetrics(formattedMetrics, (List) value, keyPrefix + "." + key);
				}
				else if (value instanceof Map) {
					addMapMetrics(formattedMetrics, (Map<String, Object>) value, keyPrefix + "." + key);
				}
				else {
					formattedMetrics.put(keyPrefix + "." + key, value);
				}
			}
		}
	}

	private void addListMetrics(Map<String, Object> formattedMetrics, List listValue, String keyPrefix) {
		if (listValue != null && !listValue.isEmpty()) {
			if (listValue.get(0) instanceof List<?>) {
				for (int i = 0; i < listValue.size(); i++) {
					addListMetrics(formattedMetrics, (List) listValue.get(i), keyPrefix + "." + i);
				}
			}
			else if (listValue.get(0) instanceof Map<?, ?>) {
				for (int i = 0; i < listValue.size(); i++) {
					Map<String, Object> metricMap = (Map<String, Object>) listValue.get(i);
					if (metricMap != null) {
						String name = null;
						Object nameValue = metricMap.get("name");
						if (nameValue != null && nameValue instanceof String) {
							name = ((String) nameValue).replaceAll("\\s", "");
						}

						if (StringUtils.isNotEmpty(name)) {
							metricMap.remove("name");
							addMapMetrics(formattedMetrics, metricMap, keyPrefix + "." + name);
						}
						else {
							addMapMetrics(formattedMetrics, metricMap, keyPrefix + "." + i);
						}
					}
				}
			}
			else {
				List<String> metricList = listValue;
				formattedMetrics.put(keyPrefix, String.join(",", metricList));
			}
		}
	}

	@Override
	public void close() {
		try {
			if (out != null) {
				out.close();
			}
			if (socket != null) {
				socket.close();
			}
		}
		catch (IOException e) {
			logger.warn("close connection to graphite error!", e);
		}
	}

	private void ensureGraphiteConnection() {
		if (socket == null) {
			synchronized (this) {
				if (socket == null) {
					try {
						logger.info("connecting to graphite(" + host + ":" + port + ")!");
						socket = new Socket(host, port);
						OutputStream s = socket.getOutputStream();
						out = new PrintWriter(s, true);
						logger.info("connect to graphite successfully!");
					}
					catch (IOException e) {
						logger.warn("connect to graphite error!", e);
					}
				}
			}
		}
	}

}
