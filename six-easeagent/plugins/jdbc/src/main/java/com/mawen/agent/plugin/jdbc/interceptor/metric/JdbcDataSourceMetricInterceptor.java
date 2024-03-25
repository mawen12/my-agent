package com.mawen.agent.plugin.jdbc.interceptor.metric;

import java.sql.Connection;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;
import com.mawen.agent.plugin.jdbc.JdbcDataSourceMetricPlugin;
import com.mawen.agent.plugin.jdbc.advice.JdbcDataSourceAdvice;
import com.mawen.agent.plugin.jdbc.support.JdbcUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = JdbcDataSourceAdvice.class, plugin = JdbcDataSourceMetricPlugin.class)
public class JdbcDataSourceMetricInterceptor implements NonReentrantInterceptor {

	private static JdbcMetric metric;
	private static final String ERR_CON_METRIC_KEY = "err-con";

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		Tags tags = JdbcMetric.newConnectionTags();
		metric = ServiceMetricRegistry.getOrCreate(config, tags, JdbcMetric.METRIC_SUPPLIER);
	}

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
	}

	@Override
	public void doAfter(MethodInfo methodInfo, Context context) {
		Connection connection = (Connection) methodInfo.getRetValue();
		String key;
		boolean success = true;
		if (methodInfo.getRetValue() == null || methodInfo.getThrowable() != null) {
			key = ERR_CON_METRIC_KEY;
			success = false;
		}
		else {
			key = JdbcUtils.getUrl(connection);
		}
		metric.collectMetric(key, success, context);
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
