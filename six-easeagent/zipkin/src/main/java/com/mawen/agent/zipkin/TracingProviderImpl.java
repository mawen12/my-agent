package com.mawen.agent.zipkin;

import brave.Tracing;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.sampler.CountingSampler;
import com.mawen.agent.config.AutoRefreshConfigItem;
import com.mawen.agent.config.ConfigAware;
import com.mawen.agent.plugin.annotation.Injection;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.api.config.ConfigConst;
import com.mawen.agent.plugin.api.trace.ITracing;
import com.mawen.agent.plugin.api.trace.TracingProvider;
import com.mawen.agent.plugin.api.trace.TracingSupplier;
import com.mawen.agent.plugin.bean.AgentInitializingBean;
import com.mawen.agent.plugin.bean.BeanProvider;
import com.mawen.agent.plugin.report.AgentReport;
import com.mawen.agent.plugin.report.tracing.ReportSpan;
import com.mawen.agent.plugin.utils.AdditionalAttributes;
import com.mawen.agent.report.AgentReportAware;
import com.mawen.agent.zipkin.impl.TracingImpl;
import com.mawen.agent.zipkin.logging.AgentMDCScopeDecorator;
import zipkin2.reporter.Reporter;
import zipkin2.reporter.brave.ConvertZipkinSpanHandler;


/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/03/20
 */
public class TracingProviderImpl implements BeanProvider, AgentReportAware, ConfigAware, AgentInitializingBean, TracingProvider {

	private Tracing tracing;
	private volatile ITracing iTracing;
	private AgentReport agentReport;
	private Config config;
	private AutoRefreshConfigItem<String> serviceName;

	@Override
	public void setConfig(Config config) {
		this.config = config;
	}

	@Override
	public void setAgentReport(AgentReport report) {
		this.agentReport = report;
	}

	@Override
	public void afterPropertiesSet() {
		ThreadLocalCurrentTraceContext traceContext = ThreadLocalCurrentTraceContext.newBuilder()
				.addScopeDecorator(AgentMDCScopeDecorator.get())
				.addScopeDecorator(AgentMDCScopeDecorator.getV2())
				.addScopeDecorator(AgentMDCScopeDecorator.getAgentDecorator())
				.build();

		serviceName = new AutoRefreshConfigItem<>(config, ConfigConst.SERVICE_NAME, Config::getString);

		Reporter<ReportSpan> reporter = span -> agentReport.report(span);
		this.tracing = Tracing.newBuilder()
				.localServiceName(getServiceName())
				.traceId128Bit(false)
				.sampler(CountingSampler.create(1))
				.addSpanHandler(new CustomTagsSpanHandler(this::getServiceName, AdditionalAttributes.getHostName()))
				.addSpanHandler(ConvertZipkinSpanHandler
						.builder(reporter)
						.alwaysReportSpans(true)
						.build()
				)
				.currentTraceContext(traceContext)
				.build();
	}

	@Injection.Bean
	public Tracing tracing() {
		return tracing;
	}

	@Override
	public TracingSupplier tracingSupplier() {
		return supplier -> {
			if (iTracing != null) {
				return iTracing;
			}

			synchronized (TracingProviderImpl.class) {
				if (iTracing != null) {
					return iTracing;
				}
				iTracing = TracingImpl.build(supplier, tracing);
			}
			return iTracing;
		};
	}

	private String getServiceName() {
		return this.serviceName.getValue();
	}
}
