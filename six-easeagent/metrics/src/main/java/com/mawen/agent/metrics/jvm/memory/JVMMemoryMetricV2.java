package com.mawen.agent.metrics.jvm.memory;

import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.regex.Pattern;

import com.mawen.agent.metrics.model.JVMMemoryGaugeMetricModel;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.MetricSubType;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.async.ScheduleHelper;
import com.mawen.agent.plugin.async.ScheduleRunner;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class JVMMemoryMetricV2 extends ServiceMetric implements ScheduleRunner {

	public static final ServiceMetricSupplier<JVMMemoryMetricV2> SUPPLIER = new ServiceMetricSupplier<>() {
		@Override
		public NameFactory newNameFactory() {
			return JVMMemoryMetricV2.nameFactory();
		}

		@Override
		public JVMMemoryMetricV2 newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return new JVMMemoryMetricV2(metricRegistry, nameFactory);
		}
	};

	private static final Pattern WHITESPACE = Pattern.compile("[\\s]+");
	private static final String POOLS = "pools";
	private static IPluginConfig config;

	private JVMMemoryMetricV2(MetricRegistry metricRegistry, NameFactory nameFactory) {
		super(metricRegistry, nameFactory);
	}

	public static JVMMemoryMetricV2 getMetric() {
		config = AutoRefreshPluginConfigRegistry.getOrCreate("observability", "jvmMemory", "metric");
		var tags = new Tags("application", "jvm-memory", "resource");

		var v2 = ServiceMetricRegistry.getOrCreate(config, tags, SUPPLIER);
		ScheduleHelper.DEFAULT.nonStopExecute(10, 10, v2::doJob);

		return v2;
	}

	public static NameFactory nameFactory() {
		return NameFactory.createBuilder()
				.gaugeType(MetricSubType.DEFAULT, new HashMap<>())
				.build();
	}

	@Override
	public void doJob() {
		if (!config.enabled()) {
			return;
		}

		var memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (var memoryPoolMXBean : memoryPoolMXBeans) {
			var memoryPoolMXBeanName = memoryPoolMXBean.getName();

			final var poolName = com.codahale.metrics.MetricRegistry.name(POOLS, WHITESPACE.matcher(memoryPoolMXBeanName).replaceAll("-"));

			var map = this.nameFactory.gaugeNames(poolName);
			for (var entry : map.entrySet()) {
				var metricName = entry.getValue();

				Gauge<JVMMemoryGaugeMetricModel> gauge = () -> new JVMMemoryGaugeMetricModel(
						memoryPoolMXBean.getUsage().getInit(),
						memoryPoolMXBean.getUsage().getUsed(),
						memoryPoolMXBean.getUsage().getCommitted(),
						memoryPoolMXBean.getUsage().getMax()
				);

				this.metricRegistry.gauge(metricName.name(), () -> gauge);
			}
		}
	}
}
