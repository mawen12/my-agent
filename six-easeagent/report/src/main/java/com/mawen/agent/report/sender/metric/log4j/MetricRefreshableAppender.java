package com.mawen.agent.report.sender.metric.log4j;

import java.util.Optional;
import java.util.function.Consumer;

import com.mawen.agent.report.metric.MetricProps;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public class MetricRefreshableAppender extends AbstractAppender implements TestableAppender {

	private final MetricProps metricProps;
	private final Configuration configuration;
	Consumer<LogEvent> logEventConsumer;
	private Appender console;
	private Appender mock;
	private final AppenderManager appenderManager;

	MetricRefreshableAppender(final String name, final MetricProps metricProps, final Configuration configuration, final AppenderManager appenderManager) {
		super(name, null, null, true, null);
		this.metricProps = metricProps;
		this.appenderManager = appenderManager;
		this.configuration = configuration;
		this.getConsoleAppender();
	}


	@Override
	public void append(LogEvent event) {
		if (!metricProps.isEnabled()) {
			return;
		}
		Optional.ofNullable(getAppender()).ifPresent(a -> a.append(event));
	}

	@Override
	public void setTestAppender(Consumer<LogEvent> logEventConsumer) {
		this.logEventConsumer = logEventConsumer;
	}

	private Appender getAppender() {
		switch (metricProps.getSenderName()) {
			case "mock": return getMockAppender();
			default: {
				return getKafkaAppender(metricProps.getTopic());
			}
		}
	}

	private Appender getMockAppender() {
		if (mock != null) {
			return mock;
		}

		mock = new AbstractAppender(this.getName() + "_mock", null, PatternLayout.createDefaultLayout(), true, null) {
			@Override
			public void append(LogEvent event) {
				Optional.ofNullable(logEventConsumer).ifPresent(l -> l.accept(event));
			}
		};
		return mock;
	}

	private Appender getKafkaAppender(String topic) {
		return Optional.ofNullable(appenderManager.appender(topic)).orElse(getConsoleAppender());
	}

	private Appender getConsoleAppender() {
		if (console != null) {
			return console;
		}

		console = ConsoleAppender.newBuilder()
				.setConfiguration(configuration)
				.setLayout(this.getLayout())
				.setName(this.getName() + "_console")
				.build();
		console.start();
		return console;
	}
}
