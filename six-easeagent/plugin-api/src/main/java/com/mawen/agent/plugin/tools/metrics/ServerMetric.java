package com.mawen.agent.plugin.tools.metrics;

import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetric;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public class ServerMetric extends ServiceMetric {

	public static final ServiceMetricSupplier<ServerMetric> SERVICE_METRIC_SUPPLIER = new ServiceMetricSupplier<ServerMetric>() {
		@Override
		public NameFactory newNameFactory() {
			return ServiceMetric.nameFactory();
		}

		@Override
		public ServerMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
			return null;
		}
	}
}
