package com.mawen.agent.plugin.api.metric;

import com.mawen.agent.plugin.api.Reporter;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.name.MetricField;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;

/**
 * A supplier of MetricRegistry interface.
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/2/23
 */
public interface MetricRegistrySupplier {

	/**
	 * new and return a MetricRegistry for.
	 * Use configure report output.
	 * <pre>{@code
	 *  config.getBoolean("enabled");
	 *  config.getInt("interval");
	 *  config.getString("topic");
	 *  config.getString("appendType");
	 * }</pre>
	 *
	 * <p>In the metric example here, all metrics are output in the form of json.
	 * All {@code tags} will also be placed in the json text in the form of key:value.
	 *
	 * <p>Different metric types have different values. The same metric will also have different values.
	 * for example:
	 * Timer can calculate count, avg, max and so on.
	 * What type and what value is calculated? What field is this value stored in json?
	 * In this method, the {@code nameFactory} is used for control.
	 * We have implemented some commonly used content by default, or we can customize our own content.
	 *
	 * <pre>{@code
	 *  NameFactory nameFactory = NameFactory.createBuilder().counterType(MetricSubType.DEFAULT,
	 *          ImmutableMap.<MetricField, MetricValueFetcher>builder()
	 *          .put(MetricField.EXECUTION_COUNT, MetricValueFetcher.CountingCount)
	 *          .build()).build();
	 *  MetricRegistry metricRegistry = supplier.newMetricRegistry(config, nameFactory, new Tags("application", "http-request", "url"));
	 *  metricRegistry.counter(nameFactory.counterName("http://127.0.0.1:8080", MetricSubType.DEFAULT)).inc();
	 * }</pre>
	 *
	 * The above code tells the calculation program:
	 * Need a Counter, this Counter calculates the value of {@link MetricField#EXECUTION_COUNT}(key="cnt"),
	 * this value is obtained using the {@link Counter#getCount()} method.
	 *
	 * <p>The output is as follows:
	 * <pre>{@code
	 *  {
	 *      "category": "application",
	 *      "type": "http-request",
	 *      "url": "http://127.0.0.1:8080",
	 *      "cnt": 1
	 *  }
	 * }</pre>
	 *
	 * @param config {@link IPluginConfig} metric config
	 * @param nameFactory {@link NameFactory} Calculation description and name description of the value of the metric.
	 * @param tags {@link Tags} tags of metric
	 * @return {@link MetricRegistry}
	 */
	MetricRegistry newMetricRegistry(IPluginConfig config, NameFactory nameFactory, Tags tags);

	/**
	 * get plugin metric reporter
	 * <pre>{@code
	 *  Reporter reporter = supplier.reporter(config);
	 *  reporter.report("{'url': 'http://127.0.0.1:8080', 'cnt': 1}");
	 * }</pre>
	 *
	 * @param config {@link IPluginConfig} metric config
	 * @return {@link Reporter}
	 */
	Reporter reporter(IPluginConfig config);
}
