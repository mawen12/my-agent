package com.mawen.agent.report.sender.metric;

import com.mawen.agent.report.metric.MetricProps;
import com.mawen.agent.report.sender.metric.log4j.AppenderManager;
import org.apache.logging.log4j.core.Logger;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class KeySender {

	private static final String CONSOLE_APPEND = "console";
	private final String key;
	private final AppenderManager appenderManager;
	private final MetricProps metricProps;
	private Logger logger;
	private org.slf4j.Logger consoleLogger;
	private boolean isConsole = false;

	public KeySender(String key, AppenderManager appenderManager, MetricProps metricProps) {
		this.key = key;
		this.appenderManager = appenderManager;
		this.metricProps = metricProps;
	}

	public void send(String content) {
		// TODO mawen
	}
}
