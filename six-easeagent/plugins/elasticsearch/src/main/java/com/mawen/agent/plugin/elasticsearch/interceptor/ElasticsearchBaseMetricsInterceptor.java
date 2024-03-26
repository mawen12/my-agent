package com.mawen.agent.plugin.elasticsearch.interceptor;

import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.MetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.ServiceMetricSupplier;
import com.mawen.agent.plugin.api.metric.name.NameFactory;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.enums.Order;

import static com.mawen.agent.plugin.api.config.ConfigConst.Namespace.*;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
public abstract class ElasticsearchBaseMetricsInterceptor extends ElasticsearchBaseInterceptor {

	protected ElasticsearchMetric elasticsearchMetric;

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		super.init(config, className, methodName, methodDescriptor);
		Tags tags = new Tags("application", ELASTICSEARCH, "index");
		RedirectProcessor.setTagsIfRedirected(Redirect.ELASTICSEARCH, tags);
		this.elasticsearchMetric = ServiceMetricRegistry.getOrCreate(config, tags, new ServiceMetricSupplier<ElasticsearchMetric>() {
			@Override
			public NameFactory newNameFactory() {
				return ElasticsearchMetric.nameFactory();
			}

			@Override
			public ElasticsearchMetric newInstance(MetricRegistry metricRegistry, NameFactory nameFactory) {
				return new ElasticsearchMetric(metricRegistry, nameFactory);
			}
		});
	}

	@Override
	public String getType() {
		return Order.METRIC.getName();
	}

	@Override
	public int order() {
		return Order.METRIC.getOrder();
	}
}
