package com.mawen.agent.metrics.jvm;

import com.mawen.agent.metrics.jvm.gc.JVMGCMetricV2;
import com.mawen.agent.metrics.jvm.memory.JVMMemoryMetricV2;
import com.mawen.agent.plugin.bean.AgentInitializingBean;
import com.mawen.agent.plugin.bean.BeanProvider;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/5
 */
public class JvmBeanProvider implements BeanProvider, AgentInitializingBean {

	public void jvmGcMetricV2() {
		JVMGCMetricV2.getMetric();
	}

	public void jvmMemoryMetricV2() {
		JVMMemoryMetricV2.getMetric();
	}

	@Override
	public int order() {
		return BeanOrder.METRIC_REGISTRY.getOrder();
	}

	@Override
	public void afterPropertiesSet() {
		jvmGcMetricV2();
		jvmMemoryMetricV2();
	}
}
