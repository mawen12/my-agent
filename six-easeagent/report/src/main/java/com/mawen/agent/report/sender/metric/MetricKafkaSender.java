package com.mawen.agent.report.sender.metric;

import java.io.IOException;
import java.util.Map;

import com.google.auto.service.AutoService;
import com.mawen.agent.config.GlobalConfigs;
import com.mawen.agent.config.report.ReportConfigConst;
import com.mawen.agent.plugin.api.config.Config;
import com.mawen.agent.plugin.report.Call;
import com.mawen.agent.plugin.report.EncodedData;
import com.mawen.agent.plugin.report.Sender;
import com.mawen.agent.report.OutputProperties;
import com.mawen.agent.report.metric.MetricProps;
import com.mawen.agent.report.plugin.NoOpCall;
import com.mawen.agent.report.sender.metric.log4j.AppenderManager;
import com.mawen.agent.report.sender.metric.log4j.RefreshableAppender;
import com.mawen.agent.report.util.Utils;
import org.apache.logging.log4j.core.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
@AutoService(Sender.class)
public class MetricKafkaSender implements Sender {

	public static final String SENDER_NAME = ReportConfigConst.METRIC_KAFKA_SENDER_NAME;
	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MetricKafkaSender.class);

	private static volatile AppenderManager appenderManager;

	private OutputProperties outputProperties;
	private MetricProps props;
	private Logger logger;
	private String prefix;


	@Override
	public String name() {
		return SENDER_NAME;
	}

	@Override
	public void init(Config config, String prefix) {
		this.prefix = prefix;
		this.outputProperties = Utils.extractOutputProperties(config);
		this.props = MetricProps.newDefault(config, prefix);
		initAppenderManager();
	}

	@Override
	public Call<Void> send(EncodedData encodedData) {
		lazyInitLogger();
		String msg = new String(encodedData.getData());
		logger.info(msg);
		return new NoOpCall<>();
	}

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void updateConfigs(Map<String, String> changes) {
		if (Utils.isOutputPropertiesChange(changes) && this.outputProperties.updateConfig(changes)) {
			appenderManager.refresh();
		}
		// check topic
		Map<String, String> cfg = this.props.asReportConfig().getConfigs();
		cfg.putAll(changes);
		MetricProps nProps = MetricProps.newDefault(new GlobalConfigs(cfg), this.prefix);
		if (nProps.getTopic().equals(this.props.getTopic())) {
			try {
				this.close();
			}
			catch (IOException e) {
				// ignored
			}
			this.props = nProps;
			this.logger = null;
			lazyInitLogger();
		}
		// check enabled
	}

	@Override
	public void close() throws IOException {
		appenderManager.stop(this.props.getTopic());
	}

	private void initAppenderManager() {
		if (appenderManager != null) {
			return;
		}

		synchronized (MetricKafkaSender.class) {
			if (appenderManager != null) {
				return;
			}
			appenderManager = AppenderManager.create(this.outputProperties);
		}
	}

	private void lazyInitLogger() {
		if (logger != null) {
			return;
		}
		String loggerName = prepareAppenderAndLogger();
		logger = com.mawen.agent.report.sender.metric.log4j.LoggerFactory.getLoggerContext().getLogger(loggerName);
	}

	private String prepareAppenderAndLogger() {
		RefreshableAppender build = RefreshableAppender.builder()
				.names(this.props.getName())
				.metricProps(this.props)
				.appenderManager(appenderManager)
				.build();
		return build.getLogger();
	}
}
