package com.mawen.agent.mock.report.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.mawen.agent.mock.report.JsonReporter;
import com.mawen.agent.mock.report.MetricFlushable;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 3.4.2
 */
public class LastJsonReporter implements JsonReporter {

	private final MetricFlushable metricFlushable;
	private final Predicate<Map<String, Object>> filter;
	private final AtomicReference<List<Map<String, Object>>> reference;

	public LastJsonReporter(Predicate<Map<String, Object>> filter, MetricFlushable metricFlushable) {
		this.filter = filter;
		this.metricFlushable = metricFlushable;
		this.reference = new AtomicReference<>();
	}

	@Override
	public void report(List<Map<String, Object>> json) {
		if (filter == null) {
			reference.set(json);
		}

		List<Map<String, Object>> result = json.stream().filter(filter).collect(Collectors.toList());
		if (!result.isEmpty()) {
			reference.set(result);
		}
	}

	public Map<String, Object> getLastOnlyOne() {
		List<Map<String, Object>> metrics = getLast();
		if (metrics.size() != 1) {
			throw new RuntimeException("metrics size is not 1 ");
		}
		return metrics.get(0);
	}

	public List<Map<String, Object>> getLast() {
		List<Map<String, Object>> metric = reference.get();
		if (metric == null || metric.isEmpty()) {
			throw new RuntimeException("metric must not be null and empty.");
		}
		return metric;
	}

	public Map<String, Object> flushAndOnlyOne() {
		List<Map<String, Object>> metrics = flushAndGet();
		if (metrics.size() != 1) {
			throw new RuntimeException("metrics size is not 1 ");
		}
		return metrics.get(0);
	}

	public List<Map<String, Object>> flushAndGet() {
		clean();
		metricFlushable.flush();
		return getLast();
	}

	public void clean() {
		reference.set(null);
	}
}
