package com.mawen.agent.metrics;

import java.util.List;

import com.mawen.agent.config.ConfigAware;
import com.mawen.agent.httpserver.nano.AgentHttpHandler;
import com.mawen.agent.httpserver.nano.AgentHttpHandlerProvider;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.metric.MetricProvider;
import com.mawen.agent.plugin.api.metric.MetricRegistrySupplier;
import com.mawen.agent.plugin.bean.BeanProvider;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.report.AgentReportAware;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class MetricBeanProviderImpl implements BeanProvider, AgentHttpHandlerProvider, ConfigAware,
		MetricProvider, AgentReportAware {

	private final MetricProviderImpl metricProvider = new MetricProviderImpl();

	@Override
	public List<AgentHttpHandler> getAgentHttpHandlers() {
		return List.of(new PrometheusAgentHttpHandler());
	}

	@Override
	public void setConfig(Config config) {
		this.metricProvider.setConfig(config);
	}

	@Override
	public MetricRegistrySupplier metricSupplier() {
		return metricProvider.metricSupplier();
	}

	@Override
	public void setAgentReport(AgentReport report) {
		this.metricProvider.setAgentReport(report);
	}

	public MetricProviderImpl getMetricProvider() {
		return metricProvider;
	}
}
