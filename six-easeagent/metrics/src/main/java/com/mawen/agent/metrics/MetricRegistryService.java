package com.mawen.agent.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.codahale.metrics.MetricRegistry;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.metrics.converter.AbstractConverter;
import com.mawen.agent.metrics.converter.AgentPrometheusExports;
import com.mawen.agent.plugin.api.metric.name.MetricName;
import com.mawen.agent.plugin.api.metric.name.Tags;
import io.prometheus.client.Collector;
import io.prometheus.client.dropwizard.samplebuilder.DefaultSampleBuilder;
import lombok.AllArgsConstructor;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricRegistryService {
	private static final Logger LOGGER = LoggerFactory.getLogger(MetricRegistryService.class);

	public static final String METRIC_TYPE_LABEL_NAME = "MetricType";
	public static final String METRIC_SUB_TYPE_LABEL_NAME = "MetricSubType";
	public static final MetricRegistryService DEFAULT = new MetricRegistryService();

	private static final List<MetricRegistry> REGISTRY_LIST = new ArrayList<>();

	public MetricRegistry createMetricRegistry(AbstractConverter abstractConverter, Supplier<Map<String, Object>> additionalAttributes, Tags tags) {
		var metricRegistry = new MetricRegistry();
		REGISTRY_LIST.add(metricRegistry);
		var agentSampleBuilder = new AgentSampleBuilder(additionalAttributes, tags);
		var agentPrometheusExports = new AgentPrometheusExports(metricRegistry, abstractConverter, agentSampleBuilder);
		agentPrometheusExports.register();
		return metricRegistry;
	}

	@AllArgsConstructor
	static class AgentSampleBuilder extends DefaultSampleBuilder {
		private final Supplier<Map<String, Object>> additionalAttributes;
		private final Tags tags;

		@Override
		public Collector.MetricFamilySamples.Sample createSample(String dropwizardName, String nameSuffix, List<String> additionalLabelNames, List<String> additionalLabelValues, double value) {
			var newAdditionalLabelNames = new ArrayList<>(additionalLabelNames);
			var newAdditionalLabelValues = new ArrayList<>(additionalLabelValues);
			additionalAttributes(newAdditionalLabelNames, newAdditionalLabelValues);
			tags(newAdditionalLabelNames, newAdditionalLabelValues);
			return super.createSample(rebuildName(dropwizardName, newAdditionalLabelNames, newAdditionalLabelValues), nameSuffix, newAdditionalLabelNames, newAdditionalLabelValues, value);
		}

		private void additionalAttributes(List<String> additionalLabelNames, List<String> additionalLabelValues) {
			if (this.additionalAttributes == null) {
				return;
			}

			var labels = additionalAttributes.get();
			if (labels == null || labels.isEmpty()) {
				return;
			}
			for (var entry : labels.entrySet()) {
				additionalLabelNames.add(entry.getKey());
				additionalLabelValues.add(entry.getValue().toString());
			}
		}

		private void tags(List<String> additionalLabelNames, List<String> additionalLabelValues) {
			if (this.tags == null) {
				return;
			}

			var other = tags.getTags();
			if (other == null || other.isEmpty()) {
				return;
			}
			for (var entry : other.entrySet()) {
				additionalLabelNames.add(entry.getKey());
				additionalLabelValues.add(entry.getValue());
			}
		}

		private String rebuildName(String name, List<String> additionalLabelNames, List<String> additionalLabelValues) {
			try {
				var metricName = MetricName.metricNameFor(name);
				var builder = new StringBuilder();
				additionalLabelNames.add(METRIC_TYPE_LABEL_NAME);
				additionalLabelValues.add(METRIC_SUB_TYPE_LABEL_NAME);

				additionalLabelNames.add(metricName.getMetricType().name());
				additionalLabelValues.add(metricName.getMetricSubType().name());

				additionalLabelNames.add(tags.getKeyFieldName());
				additionalLabelValues.add(metricName.getKey());

				builder.append(tags.getCategory());
				builder.append(".");
				builder.append(tags.getType());

				return builder.toString();
			}
			catch (Exception e) {
				LOGGER.error("rebuild metric name[{}] fail.{}",name, e);
				return name;
			}
		}
	}
}
