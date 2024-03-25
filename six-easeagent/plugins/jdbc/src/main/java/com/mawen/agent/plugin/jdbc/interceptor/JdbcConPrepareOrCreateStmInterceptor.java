package com.mawen.agent.plugin.jdbc.interceptor;

import java.sql.Connection;
import java.sql.Statement;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.field.AgentDynamicFieldAccessor;
import com.mawen.agent.plugin.field.DynamicFieldAccessor;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;
import com.mawen.agent.plugin.jdbc.JdbcTracingPlugin;
import com.mawen.agent.plugin.jdbc.advice.JdbcConnectionAdvice;
import com.mawen.agent.plugin.jdbc.support.SqlInfo;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = JdbcConnectionAdvice.class, plugin = JdbcTracingPlugin.class)
public class JdbcConPrepareOrCreateStmInterceptor implements NonReentrantInterceptor {

	private static final Logger log = LoggerFactory.getLogger(JdbcConPrepareOrCreateStmInterceptor.class);

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
		// nothing
	}

	@Override
	public void doAfter(MethodInfo methodInfo, Context context) {
		Statement stm = (Statement) methodInfo.getRetValue();
		SqlInfo sqlInfo = new SqlInfo((Connection) methodInfo.getInvoker());
		if (methodInfo.getMethod().startsWith("prepare")
				&& methodInfo.getArgs() != null
				&& methodInfo.getArgs().length > 0) {
			String sql = (String) methodInfo.getArgs()[0];
			if (sql != null) {
				sqlInfo.addSql(sql, false);
			}
		}

		if (stm instanceof DynamicFieldAccessor) {
			AgentDynamicFieldAccessor.setDynamicFieldValue(stm, sqlInfo);
		}
		else {
			log.warn("statement must implements " + DynamicFieldAccessor.class.getName());
		}
	}

	@Override
	public String getType() {
		return Order.TRACING.getName();
	}

	@Override
	public int order() {
		return Order.HIGHEST.getOrder();
	}
}
