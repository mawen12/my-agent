package com.mawen.agent.plugin.jdbc.interceptor.redirect;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.api.middleware.ResourceConfig;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.Interceptor;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.jdbc.JdbcRedirectPlugin;
import com.mawen.agent.plugin.jdbc.advice.HikariDataSourceAdvice;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 0.0.2-SNAPSHOT
 */
@AdviceTo(value = HikariDataSourceAdvice.class, plugin = JdbcRedirectPlugin.class)
public class HikariSetPropertyInterceptor implements Interceptor {
	private static final Logger log = LoggerFactory.getLogger(HikariSetPropertyInterceptor.class);

	@Override
	public void before(MethodInfo methodInfo, Context context) {
		ResourceConfig config = Redirect.DATABASE.getConfig();
		if (config == null) {
			return;
		}

		if (methodInfo.getMethod().equals("setJdbcUrl")) {
			String jdbcUrl = config.getFirstUri();
			log.info("Redirect JDBC Url: {} to {}", methodInfo.getArgs()[0], jdbcUrl);
			RedirectProcessor.redirected(Redirect.DATABASE, jdbcUrl);
		}
		else if (methodInfo.getMethod().equals("setUsername") && StringUtils.isNotEmpty(config.getUsername())) {
			log.info("Redirect JDBC Username: {} to {}", methodInfo.getArgs()[0], config.getUsername());
			methodInfo.changeArg(0, config.getUsername());
		}
		else if (methodInfo.getMethod().equals("setPassword") && StringUtils.isNotEmpty(config.getPassword())) {
			log.info("Redirect JDBC Password: *** to ***");
			methodInfo.changeArg(0, config.getPassword());
		}
	}

	@Override
	public String getType() {
		return Order.REDIRECT.getName();
	}

	@Override
	public int order() {
		return Order.REDIRECT.getOrder();
	}
}
