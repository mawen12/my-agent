package com.mawen.agent.metrics.jvm.memory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.mawen.agent.metrics.model.JVMMemoryGaugeMetricModel;
import com.mawen.agent.plugin.api.config.AutoRefreshPluginConfigRegistry;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.Gauge;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.MetricName;
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
		Tags tags = new Tags("application", "jvm-memory", "resource");

		JVMMemoryMetricV2 v2 = ServiceMetricRegistry.getOrCreate(config, tags, SUPPLIER);
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

		List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
		for (MemoryPoolMXBean memoryPoolMXBean : memoryPoolMXBeans) {
			String memoryPoolMXBeanName = memoryPoolMXBean.getName();

			final String poolName = com.codahale.metrics.MetricRegistry.name(POOLS, WHITESPACE.matcher(memoryPoolMXBeanName).replaceAll("-"));

			Map<MetricSubType, MetricName> map = this.nameFactory.gaugeNames(poolName);
			for (Map.Entry<MetricSubType, MetricName> entry : map.entrySet()) {
				MetricName metricName = entry.getValue();

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
