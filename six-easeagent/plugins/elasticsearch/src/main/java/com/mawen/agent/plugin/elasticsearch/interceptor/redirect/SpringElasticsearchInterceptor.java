package com.mawen.agent.plugin.elasticsearch.interceptor.redirect;

import java.util.ArrayList;
import java.util.List;

import com.mawen.agent.plugin.annotation.AdviceTo;
import com.mawen.agent.plugin.api.Context;
import com.mawen.agent.plugin.api.logging.Logger;
import com.mawen.agent.plugin.api.middleware.Redirect;
import com.mawen.agent.plugin.api.middleware.RedirectProcessor;
import com.mawen.agent.plugin.api.middleware.ResourceConfig;
import com.mawen.agent.plugin.bridge.Agent;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchPlugin;
import com.mawen.agent.plugin.elasticsearch.ElasticsearchRedirectPlugin;
import com.mawen.agent.plugin.elasticsearch.advice.SpringElasticsearchAdvice;
import com.mawen.agent.plugin.enums.Order;
import com.mawen.agent.plugin.interceptor.MethodInfo;
import com.mawen.agent.plugin.interceptor.NonReentrantInterceptor;
import com.mawen.agent.plugin.utils.common.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/4
 */
@AdviceTo(value = SpringElasticsearchAdvice.class, plugin = ElasticsearchRedirectPlugin.class)
public class SpringElasticsearchInterceptor implements NonReentrantInterceptor {

	private static final Logger LOGGER = Agent.getLogger(SpringElasticsearchInterceptor.class);

	@Override
	public void doBefore(MethodInfo methodInfo, Context context) {
		ResourceConfig config = Redirect.ELASTICSEARCH.getConfig();
		if (config == null) {
			return;
		}
		String method = methodInfo.getMethod();
		List<String> uris = this.formatUris(config.getUriList());
		if (method.equals("setUsername") && StringUtils.isNotEmpty(config.getUsername())) {
			LOGGER.info("Redirect Elasticsearch Username: {} to {}", methodInfo.getArgs()[0], config.getUsername());
			methodInfo.changeArg(0, config.getUsername());
		}
		else if (method.equals("setPassword") && StringUtils.isNotEmpty(config.getPassword())) {
			LOGGER.info("Redirect Elasticsearch Password: *** to ***");
			methodInfo.changeArg(0, config.getPassword());
		}
		else if (method.equals("setEndpoints") || method.equals("setUris")) {
			LOGGER.info("Redirect Elasticsearch uris: {} to {}", methodInfo.getArgs()[0], config.getUris());
			methodInfo.changeArg(0, uris);
			RedirectProcessor.redirected(Redirect.ELASTICSEARCH,config.getUris());
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

	private List<String> formatUris(List<String> uriList) {
		List<String> list = new ArrayList<>();
		for (String uri : uriList) {
			if (uri.startsWith("http://") || uri.startsWith("https://")) {
				list.add(uri);
			}
			else {
				list.add("http://" + uri);
			}
		}
		return list;
	}
}
