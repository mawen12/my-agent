package com.mawen.agent.plugin.jdbc.interceptor.metric;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.config.IPluginConfig;
import com.mawen.agent.plugin.api.metric.ServiceMetricRegistry;
import com.mawen.agent.plugin.api.metric.name.Tags;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;
import com.mawen.agent.plugin.jdbc.JdbcDataSourceMetricPlugin;
import com.mawen.agent.plugin.jdbc.advice.JdbcStatementAdvice;
import com.mawen.agent.plugin.jdbc.support.SqlInfo;
import com.mawen.agent.plugin.jdbc.support.compress.SQLCompressionFactory;
import com.mawen.agent.plugin.jdbc.support.compress.SqlCompression;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = JdbcStatementAdvice.class, plugin = JdbcDataSourceMetricPlugin.class)
public class JdbcStmMetricInterceptor implements NonReentrantInterceptor {

	private static final int MAX_CACHE_SIZE = 1000;
	private static volatile JdbcMetric metric;
	private static SqlCompression sqlCompression;
	private static Cache<String, String> cache;

	@Override
	public void init(IPluginConfig config, String className, String methodName, String methodDescriptor) {
		if (metric == null) {
			synchronized (JdbcStmMetricInterceptor.class) {
				if (metric == null) {
					Tags tags = JdbcMetric.newStatementTags();
					metric = ServiceMetricRegistry.getOrCreate(config, tags, JdbcMetric.METRIC_SUPPLIER);
					sqlCompression = SQLCompressionFactory.getSqlCompression();
					cache = CacheBuilder.newBuilder()
							.maximumSize(MAX_CACHE_SIZE)
							.removalListener(metric)
							.build();
				}
			}
		}
	}

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
	}

	@Override
	public void doAfter(MethodInfo methodInfo, Context context) {
		SqlInfo sqlInfo = context.get(SqlInfo.class);
		String sql = sqlInfo.getSql();
		String key = sqlCompression.compress(sql);
		metric.collectMetric(key, methodInfo.getThrowable() == null, context);
		String value = cache.getIfPresent(key);
		if (value == null) {
			cache.put(key, "");
		}
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
