package com.mawen.agent.report.sender.metric.log4j;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.mawen.agent.log4j2.Logger;
import com.mawen.agent.log4j2.LoggerFactory;
import com.mawen.agent.plugin.utils.common.StringUtils;
import com.mawen.agent.report.OutputProperties;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.mom.kafka.KafkaAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.layout.PatternLayout;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/3/2
 */
public interface AppenderManager {

	Appender appender(String topic);

	void stop(String topic);

	void refresh();

	static AppenderManager create(OutputProperties outputProperties) {
		return new DefaultKafkaAppenderManager(outputProperties);
	}

	static AppenderManager create(Function<String, Appender> provider) {
		return new DefaultKafkaAppenderManager(null, provider);
	}

	final class DefaultKafkaAppenderManager implements AppenderManager {

		public static final Logger LOGGER = LoggerFactory.getLogger(AppenderManager.DefaultKafkaAppenderManager.class);

		private Map<String, Appender> appenderMap = new ConcurrentHashMap<>();
		private final OutputProperties outputProperties;
		final LoggerContext context = com.mawen.agent.report.sender.metric.log4j.LoggerFactory.getLoggerContext();
		Function<String, Appender> provider;

		private DefaultKafkaAppenderManager(OutputProperties outputProperties) {
			this.outputProperties = outputProperties;
			ClassLoader initClassLoader = Thread.currentThread().getContextClassLoader();
			LOGGER.info("bind classloader: {} to AppenderManager", initClassLoader);
			this.provider = topic -> {
				ClassLoader old = Thread.currentThread().getContextClassLoader();
				Thread.currentThread().setContextClassLoader(initClassLoader);
				try {
					return this.newAppender(this.outputProperties, topic);
				}
				finally {
					Thread.currentThread().setContextClassLoader(old);
				}
			};
		}

		private DefaultKafkaAppenderManager(OutputProperties outputProperties, Function<String, Appender> provider) {
			this.outputProperties = outputProperties;
			this.provider = provider;
		}

		@Override
		public Appender appender(String topic) {
			return appenderMap.computeIfAbsent(topic,this.provider);
		}

		@Override
		public void stop(String topic) {
			Appender appender = appenderMap.remove(topic);
			if (appender != null) {
				appender.stop();
			}
		}

		@Override
		public void refresh() {
			Map<String, Appender> clearMap = this.appenderMap;
			this.appenderMap = new ConcurrentHashMap<>();
			ImmutableList<Appender> appenderList = ImmutableList.copyOf(clearMap.values());
			for (Appender a : appenderList) {
				try {
					a.stop();
				}
				catch (Exception e) {
					//
				}
			}
			clearMap.clear();
		}

		private Appender newAppender(OutputProperties outputProperties, String topic) {
			if (StringUtils.isEmpty(outputProperties.getServers())) {
				return null;
			}
			try {
				String s = RandomStringUtils.randomAscii(8);

				List<Property> propertyList = new ArrayList<>();
				propertyList.add(Property.createProperty("bootstrap.servers", outputProperties.getServers()));
				propertyList.add(Property.createProperty("timeout.ms", outputProperties.getTimeout()));
				propertyList.add(Property.createProperty("acks", "0"));
				Property.createProperty(ProducerConfig.CLIENT_ID_CONFIG, "producer_" + topic + s);
				if (SecurityProtocol.SSL.name.equals(outputProperties.getSecurityProtocol())) {
					propertyList.add(Property.createProperty(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, outputProperties.getSecurityProtocol()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, outputProperties.getSSLKeyStoreType()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_KEY_CONFIG, outputProperties.getKeyStoreKey()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_KEYSTORE_CERTIFICATE_CHAIN_CONFIG, outputProperties.getKeyStoreCertChain()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_TRUSTSTORE_CERTIFICATES_CONFIG, outputProperties.getTrustCertificate()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, outputProperties.getTrustCertificateType()));
					propertyList.add(Property.createProperty(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, outputProperties.getEndpointAlgorithm()));
				}

				Property[] properties = new Property[propertyList.size()];
				propertyList.toArray(properties);
				Appender appender = KafkaAppender.newBuilder()
						.setTopic(topic)
						.setSyncSend(false)
						.setName(topic + "_kafka_" + s)
						.setPropertyArray(properties)
						.setLayout(PatternLayout.newBuilder()
								.withCharset(StandardCharsets.UTF_8)
								.withConfiguration(context.getConfiguration())
								.withPattern("%m%n")
								.build())
						.setConfiguration(context.getConfiguration())
						.build();
				appender.start();
				return appender;
			}
			catch (Exception e) {
				LOGGER.warn("can't not create topic : {} kafka appender, error: {}", topic, e.getMessage(), e);
			}
			return null;
		}


	}
}
