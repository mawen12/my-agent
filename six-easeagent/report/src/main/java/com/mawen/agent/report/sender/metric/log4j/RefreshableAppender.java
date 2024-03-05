package com.mawen.agent.report.sender.metric.log4j;

import java.util.function.Consumer;

import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.report.metric.MetricProps;
import com.mawen.agent.report.util.TextUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.async.AsyncLoggerConfig;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDisruptor;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.LoggerConfig;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public interface RefreshableAppender extends TestableAppender {

	static Builder builder() {
		return new Builder();
	}

	String getLogger();

	class DefaultRefreshableAppender implements RefreshableAppender {
		private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRefreshableAppender.class);

		private final String loggerName;
		private final String appenderName;
		private final MetricRefreshableAppender delegate;
		private final AppenderManager appenderManager;

		DefaultRefreshableAppender(String appender, String loggerName,
		                           AppenderManager appenderManager, MetricProps metricProps) {
			this.loggerName = loggerName;
			this.appenderName = appender;
			this.appenderManager = appenderManager;
			LoggerContext context = com.mawen.agent.report.sender.metric.log4j.LoggerFactory.getLoggerContext();
			// start disruptor synchronized thread
			startAsyncDisruptor(context);
			AppenderRef[] appenderRefs = forAppenderRefs();
			LoggerConfig logger = createLogger(loggerName, context, appenderRefs);
			delegate = newDelegate(context, metricProps);
			if (delegate != null) {
				// shouldn't be null always
				logger.addAppender(delegate,Level.INFO,null);
			}
			context.getConfiguration().addLogger(loggerName, logger);
			context.updateLoggers();
		}

		@Override
		public String getLogger() {
			return loggerName;
		}

		@Override
		public void setTestAppender(Consumer<LogEvent> logEventConsumer) {
			this.delegate.setTestAppender(logEventConsumer);
		}

		private void startAsyncDisruptor(LoggerContext context) {
			AsyncLoggerConfigDisruptor asyncLoggerConfigDelegate = (AsyncLoggerConfigDisruptor) context.getConfiguration().getAsyncLoggerConfigDelegate();
			if (!asyncLoggerConfigDelegate.isStarted() && !asyncLoggerConfigDelegate.isStarting()) {
				asyncLoggerConfigDelegate.start();
			}
		}

		private AppenderRef[] forAppenderRefs() {
			return new AppenderRef[] {AppenderRef.createAppenderRef(appenderName, Level.INFO, null)};
		}

		private LoggerConfig createLogger(String loggerName, LoggerContext ctx, AppenderRef[] refs) {
			return AsyncLoggerConfig.createLogger(false, Level.INFO, loggerName,
					"true", refs, null, ctx.getConfiguration(), null);
		}

		private MetricRefreshableAppender newDelegate(LoggerContext context, MetricProps metricProps) {
			try {
				MetricRefreshableAppender metricRefreshableAppender = new MetricRefreshableAppender(this.appenderName, metricProps, context.getConfiguration(), appenderManager);
				metricRefreshableAppender.start();
				return metricRefreshableAppender;
			}
			catch (Exception e) {
				LOGGER.warn("new refreshable appender failed + [{}]",e.getMessage());
			}
			return null;
		}
	}

	class Builder {
		private String appender;
		private String loggerName;
		private AppenderManager appenderManager;
		private MetricProps metricProps;

		public Builder names(String prefix) {
			if (TextUtils.isEmpty(prefix)) {
				prefix = "DefaultPrefix";
			}
			this.appender = prefix.concat("MetricAppender");
			this.loggerName = prefix;
			return this;
		}

		public Builder appenderManager(AppenderManager manager) {
			this.appenderManager = manager;
			return this;
		}

		public Builder metricProps(MetricProps props) {
			this.metricProps = props;
			return this;
		}

		public RefreshableAppender build() {
			if (TextUtils.isEmpty(appender) || TextUtils.isEmpty(loggerName) || appenderManager == null) {
				throw new IllegalArgumentException("appender, loggerName must be a unique name, kafkaAppenderManager can't be null");
			}
			return new DefaultRefreshableAppender(
					appender,
					loggerName,
					appenderManager,
					metricProps
			);
		}
	}
}
